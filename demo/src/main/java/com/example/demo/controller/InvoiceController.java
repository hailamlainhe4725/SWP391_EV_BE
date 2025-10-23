package com.example.demo.controller;

import com.example.demo.dto.request.CreateInvoiceRequest;
import com.example.demo.dto.response.InvoiceResponse;
import com.example.demo.dto.response.MonthlyInvoiceSummaryResponse;
import com.example.demo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // === STAFF: tạo hóa đơn ===
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/createInvoice")
    public ResponseEntity<List<InvoiceResponse>> create(@RequestBody CreateInvoiceRequest req) {
        return ResponseEntity.ok(invoiceService.createAutoInvoicesByEmail(req.getEmail()));
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
public ResponseEntity<MonthlyInvoiceSummaryResponse> getMyInvoice(
        Authentication authentication,
        @RequestParam(required = false) String month // ví dụ: "2025-10"
) {
    YearMonth targetMonth = (month != null) ? YearMonth.parse(month) : null;
    return ResponseEntity.ok(invoiceService.getMyInvoice(authentication, targetMonth));
}

}
