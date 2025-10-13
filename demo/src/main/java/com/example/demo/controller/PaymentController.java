package com.example.demo.controller;

import com.example.demo.dto.request.PaymentRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // === USER: tạo thanh toán ===
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/createPayment")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest req) {
        return ResponseEntity.ok(paymentService.createPayment(req));
    }

    // === USER: xem lịch sử thanh toán của mình ===
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(@RequestParam Long userId) {
        List<PaymentResponse> res = paymentService.getAll().stream()
                .filter(p -> p.getInvoiceId().equals(userId))
                .toList();
        return ResponseEntity.ok(res);
    }

    // === STAFF: xem tất cả thanh toán ===
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAll());
    }
}
