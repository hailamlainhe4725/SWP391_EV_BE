package com.example.demo.controller;

import com.example.demo.dto.response.InvoiceResponse;
import com.example.demo.dto.response.SumaInvoiceResponse;
import com.example.demo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

    /**
     * 🔹 Lấy tất cả hóa đơn (dành cho admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    /**
     * 🔹 Lấy tổng hợp hóa đơn của người dùng đang đăng nhập trong tháng chỉ định
     * Nếu không truyền `month`, mặc định là tháng hiện tại.
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public SumaInvoiceResponse getMyInvoice(
            Authentication authentication,
            @RequestParam(value = "month", required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return invoiceService.getMyInvoice(authentication, month);
    }

    /**
     * 🔹 Tạo hóa đơn tự động cho user theo email (dành cho hệ thống hoặc admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auto")
    public SumaInvoiceResponse createAutoInvoicesByEmail(@RequestParam String email) {
        return invoiceService.createAutoInvoicesByEmail(email);
    }

    /**
     * 🔹 Lấy tất cả SumaInvoice (gộp hóa đơn hàng tháng của tất cả user)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/suma")
    public List<SumaInvoiceResponse> getAllSumaInvoices() {
        return invoiceService.getAllSumaInvoices();
    }
}
