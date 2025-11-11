package com.example.demo.controller;

import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // =========================
    // Tạo link thanh toán
    // =========================
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create/pay")
    public ResponseEntity<?> createPayment(@RequestParam Long SumaInvoiceId) {
        try {
            return ResponseEntity.ok(paymentService.createPaymentLink(SumaInvoiceId));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // =========================
    // Xử lý Webhook PayOS (phiên bản 1.0.3)
    // =========================
    @PostMapping("/webhook")
public ResponseEntity<String> handleWebhook(@RequestBody(required = false) String requestBody) {
    System.out.println("📩 Received webhook request");

    // ✅ Bước 1: PayOS gửi test webhook (body rỗng)
    if (requestBody == null || requestBody.isEmpty()|| requestBody.equals("{}")) {
        System.out.println("🔹 PayOS sent webhook test request");
        return ResponseEntity.ok("Webhook test OK");
    }

    try {
        // ✅ Bước 2: PayOS gửi webhook thật sau khi thanh toán
        System.out.println("🔹 Webhook raw body: " + requestBody);

        // Gọi sang PaymentService để xử lý thật
        paymentService.handleWebhook(requestBody);

        return ResponseEntity.ok("Webhook processed successfully");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body("Invalid webhook: " + e.getMessage());
    }
}


}
