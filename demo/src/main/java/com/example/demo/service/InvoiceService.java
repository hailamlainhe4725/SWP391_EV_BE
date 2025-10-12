package com.example.demo.service;

import com.example.demo.dto.request.CreateInvoiceRequest;
import com.example.demo.dto.response.InvoiceDetailResponse;
import com.example.demo.dto.response.InvoiceResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.BillingStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InvoiceDetailRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

        private final InvoiceRepository invoiceRepository;
        private final InvoiceDetailRepository detailRepository;
        private final UserRepository userRepository;
        private final VehicleRepository vehicleRepository;

        public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
                User user = userRepository.findById(req.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

                Invoice invoice = Invoice.builder()
                                .user(user)
                                .vehicle(vehicle)
                                .status(BillingStatus.OPEN)
                                .issuedDate(LocalDateTime.now())
                                .dueDate(LocalDateTime.now().plusDays(7))
                                .note(req.getNote())
                                .build();

                invoiceRepository.save(invoice);

                double total = 0;
                for (var d : req.getDetails()) {
                        InvoiceDetail detail = InvoiceDetail.builder()
                                        .invoice(invoice)
                                        .feeType(d.getFeeType())
                                        .sourceType(d.getSourceType())
                                        .description(d.getDescription())
                                        .amount(d.getAmount().doubleValue())
                                        .build();
                        detailRepository.save(detail);
                        total += d.getAmount().doubleValue();
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

        private InvoiceResponse mapToResponse(Invoice i) {
                List<InvoiceDetailResponse> details = detailRepository.findByInvoice(i).stream()
                                .map(d -> InvoiceDetailResponse.builder()
                                                .feeType(d.getFeeType())
                                                .description(d.getDescription())
                                                .amount(d.getAmount())
                                                .build())
                                .collect(Collectors.toList());

                return InvoiceResponse.builder()
                                .invoiceId(i.getInvoiceId())
                                .userId(i.getUser().getId())
                                .vehicleId(i.getVehicle().getVehicleId())
                                .status(i.getStatus())
                                .totalAmount(i.getTotalAmount())
                                .issuedDate(i.getIssuedDate())
                                .dueDate(i.getDueDate())
                                .details(details)
                                .build();
        }
}
