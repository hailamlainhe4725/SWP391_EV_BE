package com.example.demo.service;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.Payment;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public Payment create(Integer invoiceId, Payment payment) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        payment.setInvoice(invoice);
        payment.setStatus("Pending");
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment updateStatus(Integer id, String status) {
        Payment p = getById(id);
        p.setStatus(status);
        return paymentRepository.save(p);
    }

    public Payment getById(Integer id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }
}
