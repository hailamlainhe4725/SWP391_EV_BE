package com.example.demo.service;

import com.example.demo.dto.request.PaymentRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.Payment;
import com.example.demo.enums.BillingStatus;
import com.example.demo.enums.TransactionStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final InvoiceRepository invoiceRepository;

        public PaymentResponse createPayment(PaymentRequest req) {
                Invoice invoice = invoiceRepository.findById(req.getInvoiceId())
                                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

                Payment payment = Payment.builder()
                                .invoice(invoice)
                                .paidAmount(req.getPaidAmount())
                                .method(req.getMethod())
                                .status(TransactionStatus.SUCCESS)
                                .paymentDate(LocalDateTime.now())
                                .build();

                paymentRepository.save(payment);

                invoice.setStatus(BillingStatus.SETTLED);
                invoiceRepository.save(invoice);

                return mapToResponse(payment);
        }

        public List<PaymentResponse> getAll() {
                return paymentRepository.findAll().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
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
