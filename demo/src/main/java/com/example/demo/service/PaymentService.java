package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.enums.BillingStatus;
import com.example.demo.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayOS payOS;
    private final SumaInvoiceRepository sumaInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ObjectMapper objectMapper;

    // =========================
    // 🔹 Tạo link thanh toán cho SumaInvoice
    // =========================
    public CheckoutResponseData createPaymentLink(Long sumaInvoiceId) throws Exception {
        try{
        SumaInvoice sumaInvoice = sumaInvoiceRepository.findById(sumaInvoiceId)
                .orElseThrow(() -> new RuntimeException("SumaInvoice not found"));

        // Tạo orderCode riêng cho giao dịch này
        long orderCode = System.currentTimeMillis();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(sumaInvoice.getTotalAmount().intValue())
                .description("Payment" + sumaInvoice.getMonth())
                .returnUrl("https://caleb-idiomatic-milissa.ngrok-free.dev/owner/success")
                .cancelUrl("https://your-frontend.com/payment-cancel")
                .build();

        CheckoutResponseData response = payOS.createPaymentLink(paymentData);

        // Lưu Payment vào DB
        Payment payment = Payment.builder()
                .sumaInvoice(sumaInvoice)
                .user(sumaInvoice.getUser())
                .amount(sumaInvoice.getTotalAmount())
                .orderCode(String.valueOf(orderCode))
                .checkoutUrl(response.getCheckoutUrl())
                .qrCode(response.getQrCode())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        return response;
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    // =========================
    // 🔹 Xử lý webhook PayOS 1.0.3 (SumaInvoice)
    // =========================
    public void handleWebhook(String requestBody) throws Exception {
        System.out.println("=== 🔔 PAYOS WEBHOOK RECEIVED ===");
        System.out.println(requestBody);

        JsonNode root = objectMapper.readTree(requestBody);
        JsonNode data = root.get("data");

        if (data == null) throw new RuntimeException("Invalid webhook format: missing data");

        long orderCode = data.get("orderCode").asLong();
        double amount = data.get("amount").asDouble();
        String code = data.get("code").asText();

        System.out.println("✅ orderCode=" + orderCode);
        System.out.println("✅ amount=" + amount);
        System.out.println("✅ code=" + code);

        // ✅ Thanh toán thành công
        if ("00".equals(code)) {
            Payment payment = paymentRepository.findByOrderCode(String.valueOf(orderCode))
                    .orElseThrow(() -> new RuntimeException("Payment not found with orderCode: " + orderCode));

            payment.setStatus("SUCCESS");
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Cập nhật trạng thái SumaInvoice
            SumaInvoice sumaInvoice = payment.getSumaInvoice();
            sumaInvoice.setStatus(BillingStatus.SETTLED);
            sumaInvoice.setUpdatedAt(LocalDateTime.now());
            sumaInvoiceRepository.save(sumaInvoice);



            System.out.println("✅ Payment + SumaInvoice + all child Invoices updated successfully");
        } else {
            System.out.println("⚠️ Payment not completed, code = " + code);
        }
    }
}
