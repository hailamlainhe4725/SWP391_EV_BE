package com.example.demo.service;

import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.Payment;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public List<PaymentResponse> getByInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return paymentRepository.findByInvoice(invoice).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse create(Long invoiceId, Payment payment) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        payment.setInvoice(invoice);
        return mapToResponse(paymentRepository.save(payment));
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getPaymentId())
                .invoiceId(p.getInvoice().getInvoiceId())
                .amount(p.getPaidAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .paymentDate(p.getPaymentDate())
                .build();
    }
}
