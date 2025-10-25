package com.example.demo.service;

import com.example.demo.entity.Invoice;
import com.example.demo.entity.Payment;
import com.example.demo.enums.BillingStatus;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PayOS payOS;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // Tạo link thanh toán
    // =========================
    public CheckoutResponseData createPaymentLink(Long invoiceId) throws Exception {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Tạo orderCode riêng cho giao dịch này (có thể là timestamp hoặc paymentId sau khi lưu)
        long orderCode = System.currentTimeMillis();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(invoice.getTotalAmount().intValue())
                .description("Payment for invoice #" + invoiceId)
                .returnUrl("https://your-frontend.com/payment-success")
                .cancelUrl("https://your-frontend.com/payment-cancel")
                .build();

        CheckoutResponseData response = payOS.createPaymentLink(paymentData);

        // Lưu bản ghi Payment vào DB
        Payment payment = Payment.builder()
                .invoice(invoice)
                .user(invoice.getUser()) // nếu Invoice có thuộc tính user
                .amount(invoice.getTotalAmount())
                .orderCode(String.valueOf(orderCode))
                .checkoutUrl(response.getCheckoutUrl())
                .qrCode(response.getQrCode())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        return response;
    }

    // =========================
    // Xử lý Webhook (PayOS 1.0.3)
    // =========================
 public void handleWebhook(String requestBody) throws Exception {
    System.out.println("=== 🔔 PAYOS WEBHOOK RECEIVED ===");
    System.out.println(requestBody);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(requestBody);
    JsonNode data = root.get("data");

    if (data == null) throw new RuntimeException("Invalid webhook format: missing data");

    long orderCode = data.get("orderCode").asLong();
    double amount = data.get("amount").asDouble();
    String code = data.get("code").asText();

    System.out.println("✅ orderCode=" + orderCode);
    System.out.println("✅ amount=" + amount);
    System.out.println("✅ code=" + code);

    // ✅ Xác định thanh toán thành công (code == "00")
    if ("00".equals(code)) {
        Payment payment = paymentRepository.findByOrderCode(String.valueOf(orderCode))
                .orElseThrow(() -> new RuntimeException("Payment not found with orderCode: " + orderCode));

        payment.setStatus("SUCCESS");
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Invoice invoice = payment.getInvoice();
        invoice.setStatus(BillingStatus.SETTLED);
        invoiceRepository.save(invoice);

        System.out.println("✅ Payment + Invoice updated successfully");
    } else {
        System.out.println("⚠️ Payment not completed, code = " + code);
    }
}

}
