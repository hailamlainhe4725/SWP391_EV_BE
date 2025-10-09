package com.example.demo.service;

import com.example.demo.dto.response.InvoiceDetailResponse;
import com.example.demo.dto.response.InvoiceResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
        private final InvoiceRepository invoiceRepository;
        private final InvoiceDetailRepository invoiceDetailRepository;
        private final UserRepository userRepository;

        public List<InvoiceResponse> getByUserEmail(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                return invoiceRepository.findByUser(user).stream()
                                .filter(i -> !i.isDeleted())
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<InvoiceResponse> getAll() {
                return invoiceRepository.findAll().stream()
                                .filter(i -> !i.isDeleted())
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public InvoiceResponse getById(Long id) {
                Invoice invoice = invoiceRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
                invoice.setDetails(invoiceDetailRepository.findByInvoice(invoice));
                return mapToResponse(invoice);
        }

        public void softDelete(Long id) {
                Invoice invoice = invoiceRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
                invoice.setDeleted(true);
                invoiceRepository.save(invoice);
        }

        private InvoiceResponse mapToResponse(Invoice invoice) {
                List<InvoiceDetail> details = invoice.getDetails() != null
                                ? invoice.getDetails()
                                : invoiceDetailRepository.findByInvoice(invoice);

                double total = details.stream()
                                .filter(d -> !d.isDeleted())
                                .mapToDouble(InvoiceDetail::getAmount)
                                .sum();

                return InvoiceResponse.builder()
                                .invoiceId(invoice.getInvoiceId())
                                .userId(invoice.getUser().getId())
                                .vehicleId(invoice.getVehicle().getVehicleId())
                                .status(invoice.getStatus())
                                .totalAmount(total)
                                .issuedDate(invoice.getIssuedDate())
                                .dueDate(invoice.getDueDate())
                                .details(details.stream().map(d -> InvoiceDetailResponse.builder()
                                                .feeType(d.getFeeType())
                                                .amount(d.getAmount())
                                                .description(d.getDescription())
                                                .build()).collect(Collectors.toList()))
                                .build();
        }
}
