package com.example.demo.service;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceDetail;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public Invoice createInvoice(Long userId, Long vehicleId,
            List<InvoiceDetail> details) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Invoice inv = Invoice.builder()
                .user(user)
                .vehicle(vehicle)
                .issuedDate(LocalDateTime.now())
                .status("Pending")
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceDetail d : details) {
            d.setInvoice(inv);
            total = total.add(BigDecimal.valueOf(d.getAmount()));
        }
        inv.setTotalAmount(total.doubleValue());
        Invoice saved = invoiceRepository.save(inv);
        saved.setDetails(details);
        return saved;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoicesByUser(Long userId) {
        return invoiceRepository.findByUserUserId(userId);
    }

    public Optional<Invoice> getInvoiceById(Integer id) {
        return invoiceRepository.findById(id);
    }

    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id.intValue());
    }

    public Invoice getById(Integer id) {
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    public List<Invoice> getByUser(Long userId) {
        return invoiceRepository.findByUserUserId(userId);
    }
}
