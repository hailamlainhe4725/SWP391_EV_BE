package com.example.demo.controller;

import com.example.demo.dto.request.CreateInvoiceRequest;
import com.example.demo.dto.response.InvoiceResponse;
import com.example.demo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // === STAFF: tạo hóa đơn ===
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/createInvoice")
    public ResponseEntity<InvoiceResponse> create(@RequestBody CreateInvoiceRequest req) {
        return ResponseEntity.ok(invoiceService.createInvoice(req));
    }

    // === STAFF: xem tất cả hóa đơn ===
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/all")
    public ResponseEntity<List<InvoiceResponse>> getAll() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    // === USER: xem hóa đơn của mình ===
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(@RequestParam Long userId) {
        return ResponseEntity.ok(
                invoiceService.getAllInvoices().stream()
                        .filter(i -> i.getUserId().equals(userId))
                        .toList());
    }
}
