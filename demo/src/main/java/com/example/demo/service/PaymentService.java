package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Payment;
import com.example.demo.entity.SumaInvoice;
import com.example.demo.enums.BillingStatus;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.SumaInvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayOS payOS;
    private final SumaInvoiceRepository sumaInvoiceRepository;
    private final PaymentRepository paymentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser

    // Tạo Payment Link
    public CreatePaymentLinkResponse createPaymentLink(Long sumaInvoiceId) throws PayOSException {
        SumaInvoice sumaInvoice = sumaInvoiceRepository.findById(sumaInvoiceId)
                .orElseThrow(() -> new RuntimeException("SumaInvoice not found"));

        long orderCode = System.currentTimeMillis() / 1000;

        CreatePaymentLinkRequest req = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(sumaInvoice.getTotalAmount().longValue())
                .description("Thanh toán tháng " + sumaInvoice.getMonth())
                .returnUrl("https://caleb-idiomatic-milissa.ngrok-free.dev/owner/invoice")
                .cancelUrl("https://caleb-idiomatic-milissa.ngrok-free.dev/owner/invoice")
                .build();

        CreatePaymentLinkResponse resp = payOS.paymentRequests().create(req);

        Payment payment = Payment.builder()
                .sumaInvoice(sumaInvoice)
                .user(sumaInvoice.getUser())
                .amount(sumaInvoice.getTotalAmount())
                .orderCode(String.valueOf(orderCode))
                .checkoutUrl(resp.getCheckoutUrl())
                .qrCode(resp.getQrCode())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
        return resp;
    }

    // Xử lý webhook
   public void processWebhook(String jsonBody) throws Exception {
    System.out.println("📩 RAW webhook: " + jsonBody);

    // Parse JSON webhook
    Map<String, Object> webhookData = objectMapper.readValue(jsonBody, Map.class);

    // Lấy data map từ webhook, kiểm tra null
    Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
    if (data == null) {
        throw new RuntimeException("Webhook 'data' field is missing");
    }

    // Lấy các trường từ data, kiểm tra null
    Object orderCodeObj = data.get("orderCode");
    if (orderCodeObj == null) {
        throw new RuntimeException("Webhook 'orderCode' is missing");
    }
    String orderCode = String.valueOf(orderCodeObj);

    String code = (String) data.get("code");
    if (code == null) {
        throw new RuntimeException("Webhook 'code' is missing");
    }

    Number amountNum = (Number) data.get("amount");
    long amount = amountNum != null ? amountNum.longValue() : 0L;

    // Lấy Payment từ database
    Payment payment = paymentRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    if ("00".equals(code)) { // Payment thành công
        payment.setStatus("SUCCESS");
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        SumaInvoice invoice = payment.getSumaInvoice();
        invoice.setStatus(BillingStatus.SETTLED);
        invoice.setUpdatedAt(LocalDateTime.now());
        sumaInvoiceRepository.save(invoice);

        System.out.println("✔ PAYMENT UPDATED");
    } else { // Payment thất bại
        payment.setStatus("FAILED");
        paymentRepository.save(payment);
    }
}

}
