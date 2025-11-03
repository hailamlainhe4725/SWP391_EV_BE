package com.example.demo.service;

import com.example.demo.dto.response.InvoiceDetailResponse;
import com.example.demo.dto.response.InvoiceResponse;
import com.example.demo.dto.response.SumaInvoiceResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.BillingStatus;
import com.example.demo.enums.FixFeeType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository detailRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final FixedFeeRepository fixedFeeRepository;
    private final VariableFeeRepository variableFeeRepository;
    private final OwnershipRepository ownershipRepository;
    private final SumaInvoiceRepository sumaInvoiceRepository;

    /**
     * 🔹 Tạo toàn bộ hóa đơn tự động theo email người dùng, gắn với SumaInvoice
     */
@Transactional
public SumaInvoiceResponse createAutoInvoicesByEmail(String email) {
    try {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Ownership> ownerships = ownershipRepository.findByUser_IdAndDeletedFalse(user.getId());
        if (ownerships.isEmpty()) {
            throw new RuntimeException("User does not own any vehicles.");
        }

        String currentMonth = YearMonth.now().toString();

        // ✅ Nếu đã có SumaInvoice trong tháng này, return luôn
        Optional<SumaInvoice> existing = sumaInvoiceRepository.findByUserAndMonth(user, currentMonth);
        if (existing.isPresent()) {
            return mapToSumaInvoiceResponse(existing.get());
        }

        // ✅ Tạo SumaInvoice mới
        SumaInvoice sumaInvoice = SumaInvoice.builder()
                .user(user)
                .month(currentMonth)
                .totalAmount(0.0)
                .status(BillingStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        sumaInvoice = sumaInvoiceRepository.save(sumaInvoice);

        double totalAmount = 0;

        // ✅ Tạo hóa đơn cho từng xe (mỗi lần tạo nằm trong transaction riêng)
        for (Ownership own : ownerships) {
            Vehicle vehicle = own.getVehicle();
            try {
                InvoiceResponse response = createAutoInvoiceInNewTransaction(
                        user.getId(),
                        vehicle.getVehicleId(),
                        "Auto invoice for " + vehicle.getModel() + " - " + vehicle.getPlateNumber()
                );

                // ✅ Gắn invoice vào SumaInvoice
                Invoice invoice = invoiceRepository.findById(response.getInvoiceId())
                        .orElseThrow(() -> new RuntimeException("Created invoice not found"));
                invoice.setSumaInvoice(sumaInvoice);
                invoiceRepository.save(invoice);

                totalAmount += response.getTotalAmount();

            } catch (Exception ex) {
                ex.printStackTrace();
                // ❗ Không throw lại → tránh rollback toàn bộ transaction
                System.out.println("⚠️ Skipped vehicle " + vehicle.getPlateNumber() + " due to error: " + ex.getMessage());
            }
        }

        // ✅ Cập nhật tổng
        sumaInvoice.setTotalAmount(totalAmount);
        sumaInvoice.setUpdatedAt(LocalDateTime.now());
        sumaInvoiceRepository.save(sumaInvoice);

        return mapToSumaInvoiceResponse(sumaInvoice);

    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to create invoices: " + e.getMessage(), e);
    }
}

    /**
     * 🔹 Tạo hóa đơn tự động cho 1 user + 1 vehicle
     */
   @Transactional(propagation = Propagation.REQUIRES_NEW)
public InvoiceResponse createAutoInvoiceInNewTransaction(Long userId, Long vehicleId, String note) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
Vehicle vehicle = vehicleRepository.findByIdAndDeletedFalse(vehicleId)
    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or deleted"));

    Ownership ownership = ownershipRepository.findByUser_IdAndVehicle_VehicleId(user.getId(), vehicle.getVehicleId())
    .orElseThrow(() -> new ResourceNotFoundException("ownership not found"));

    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    LocalDateTime endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

    boolean exists = invoiceRepository.existsByUserAndVehicleAndIssuedDateBetween(user, vehicle, startOfMonth, endOfMonth);
    if (exists) {
        throw new RuntimeException("Invoice for this month already exists.");
    }

    Invoice invoice = Invoice.builder()
            .user(user)
            .vehicle(vehicle)
            .issuedDate(LocalDateTime.now())
            .dueDate(LocalDateTime.now().plusDays(7))
            .note(note)
            .totalAmount(0.0)
            .build();

    invoice = invoiceRepository.save(invoice);

    double total = 0;
    // ✅ Fixed Fees
    List<FixedFee> fixedFees = fixedFeeRepository.findByVehicle(vehicle);

    for (FixedFee ff : fixedFees) {
           
        InvoiceDetail detail = InvoiceDetail.builder()
                .invoice(invoice)
                .sourceType("Fixed")
                .relatedId(ff.getFixedFeeId())
                .feeType(ff.getType().name())
                .description(ff.getDescription())
                .amount(ff.getBaseAmount()*ownership.getTotalSharePercentage()*0.01)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
        detailRepository.save(detail);
        total += ff.getBaseAmount()*ownership.getTotalSharePercentage()*0.01;
    }

    // ✅ Variable Fees
    List<VariableFee> variableFees = variableFeeRepository.findByVehicleAndUserAndDeletedFalseAndCreatedAtBetween(vehicle, user, startOfMonth, endOfMonth);
    for (VariableFee vf : variableFees) {
        InvoiceDetail detail = InvoiceDetail.builder()
                .invoice(invoice)
                .sourceType("Variable")
                .relatedId(vf.getVariableFeeId())
                .feeType(vf.getType().name())
                .description(vf.getDescription())
                .amount(vf.getAmount())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
        detailRepository.save(detail);
        total += vf.getAmount();
    }

    invoice.setTotalAmount(total);
    invoiceRepository.save(invoice);

    System.out.println("✅ Created invoice #" + invoice.getInvoiceId() + " total=" + total);
    return mapToResponse(invoice);
}

    /**
     * 🔹 Lấy toàn bộ Invoice (nếu cần)
     */
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 🔹 Lấy toàn bộ SumaInvoice (tổng hợp theo tháng)
     */
    public List<SumaInvoiceResponse> getAllSumaInvoices() {
        return sumaInvoiceRepository.findAll().stream()
                .map(this::mapToSumaInvoiceResponse)
                .collect(Collectors.toList());
    }

    /**
     * 🔹 Lấy SumaInvoice theo user và tháng (hoặc auto tạo nếu chưa có)
     */
    public SumaInvoiceResponse getMyInvoice(Authentication authentication, YearMonth targetMonth) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (targetMonth == null) {
            targetMonth = YearMonth.now();
        }

        String monthStr = targetMonth.toString();

        return sumaInvoiceRepository.findByUserAndMonth(user, monthStr)
                .map(this::mapToSumaInvoiceResponse)
                .orElseGet(() -> createAutoInvoicesByEmail(user.getEmail()));
    }

    /**
     * 🔹 Mapping
     */
    private SumaInvoiceResponse mapToSumaInvoiceResponse(SumaInvoice sumaInvoice) {
        List<InvoiceResponse> invoiceResponses = sumaInvoice.getInvoices() != null
                ? sumaInvoice.getInvoices().stream().map(this::mapToResponse).toList()
                : List.of();

        return SumaInvoiceResponse.builder()
                .sumaInvoiceId(sumaInvoice.getId())
                .userName(sumaInvoice.getUser().getFullName())
                .month(sumaInvoice.getMonth())
                .totalAmount(sumaInvoice.getTotalAmount())
                .status(sumaInvoice.getStatus().name())
                .invoices(invoiceResponses)
                .build();
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        List<InvoiceDetail> invoiceDetails = invoice.getDetails();
        if (invoiceDetails == null) {
            invoiceDetails = List.of();
        }

        List<InvoiceDetailResponse> details = new ArrayList<>();
        for (InvoiceDetail detail : invoiceDetails) {
            if (detail == null || detail.isDeleted()) continue;
            details.add(InvoiceDetailResponse.builder()
                    .detailId(detail.getDetailId())
                    .feeType(detail.getFeeType())
                    .sourceType(detail.getSourceType())
                    .relatedId(detail.getRelatedId())
                    .description(detail.getDescription())
                    .amount(detail.getAmount())
                    .createdAt(detail.getCreatedAt())
                    .build());
        }

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .userId(invoice.getUser() != null ? invoice.getUser().getId() : null)
                .fullName(invoice.getUser().getFullName())
                .email(invoice.getUser().getEmail())
                .phone(invoice.getUser().getPhone())
                .plateNumber(invoice.getVehicle().getPlateNumber())
                .model(invoice.getVehicle().getModel())
                .vehicleId(invoice.getVehicle() != null ? invoice.getVehicle().getVehicleId() : null)
                .totalAmount(invoice.getTotalAmount())
                .issuedDate(invoice.getIssuedDate())
                .dueDate(invoice.getDueDate())
                .details(details)
                .build();
    }
}
