package com.example.demo.service;

import com.example.demo.dto.request.CreateInvoiceRequest;
import com.example.demo.dto.response.InvoiceDetailResponse;
import com.example.demo.dto.response.InvoiceResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.BillingStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Tạo hóa đơn tự động — chỉ cần userId và vehicleId.
     * Toàn bộ fee sẽ được lấy tự động từ bảng FixedFee và VariableFee.
     */
    public InvoiceResponse createAutoInvoice(CreateInvoiceRequest req) {

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

         // Xác định đầu và cuối tháng hiện tại
    LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        
    Invoice invoice = Invoice.builder()
                .user(user)
                .vehicle(vehicle)
                .status(BillingStatus.OPEN)
                .issuedDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .note(req.getNote())
                .totalAmount(0.0)
                .build();

        invoiceRepository.save(invoice);

        double total = 0;

        // 1️⃣ Lấy Fixed Fees theo Vehicle
        List<FixedFee> fixedFees = fixedFeeRepository.findByVehicleAndDeletedFalseAndCreatedAtBetween(vehicle,startOfMonth,endOfMonth);
        for (FixedFee ff : fixedFees) {
            InvoiceDetail detail = InvoiceDetail.builder()
                    .invoice(invoice)
                    .sourceType("Fixed")
                    .relatedId(ff.getFixedFeeId())
                    .feeType(ff.getType().name())
                    .description(ff.getDescription())
                    .amount(ff.getBaseAmount())
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build();
            detailRepository.save(detail);
            total += ff.getBaseAmount();
        }

        // 2️⃣ Lấy Variable Fees theo Vehicle + User
        List<VariableFee> variableFees = variableFeeRepository.findByVehicleAndUserAndDeletedFalseAndCreatedAtBetween(vehicle, user,startOfMonth,endOfMonth);
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

        return mapToResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

   private InvoiceResponse mapToResponse(Invoice invoice) {
    // đảm bảo không null
    List<InvoiceDetail> invoiceDetails = invoice.getDetails();
    if (invoiceDetails == null) {
        invoiceDetails = List.of();
    }

    // dùng for-loop thay vì stream().map(...) để tránh mọi vấn đề inference
    List<InvoiceDetailResponse> details = new ArrayList<>();
    for (InvoiceDetail detail : invoiceDetails) {
        if (detail == null) continue;
        if (detail.isDeleted()) continue;

        InvoiceDetailResponse.InvoiceDetailResponseBuilder builder = InvoiceDetailResponse.builder();
        builder.detailId(detail.getDetailId());
        builder.feeType(detail.getFeeType());
        builder.sourceType(detail.getSourceType());
        builder.relatedId(detail.getRelatedId());
        builder.description(detail.getDescription());
        builder.amount(detail.getAmount());
        builder.createdAt(detail.getCreatedAt());

        InvoiceDetailResponse dto = builder.build();
        details.add(dto);
    }

    return InvoiceResponse.builder()
            .invoiceId(invoice.getInvoiceId())
            .userId(invoice.getUser() != null ? invoice.getUser().getId() : null)
            .vehicleId(invoice.getVehicle() != null ? invoice.getVehicle().getVehicleId() : null)
            .status(invoice.getStatus())
            .totalAmount(invoice.getTotalAmount())
            .issuedDate(invoice.getIssuedDate())
            .dueDate(invoice.getDueDate())
            .details(details)
            .build();
}

}