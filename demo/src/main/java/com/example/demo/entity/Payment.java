package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long paymentId;

    // 🔹 Giao dịch này gắn với SumaInvoice (hóa đơn tổng hợp của tháng)
    @ManyToOne
    @JoinColumn(name = "suma_invoice_id", nullable = false)
    SumaInvoice sumaInvoice;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    Double amount;
    String orderCode; // Mã giao dịch gửi lên PayOS
    String checkoutUrl;
    String qrCode;
    String status; // PENDING, SUCCESS, FAILED

    LocalDateTime createdAt = LocalDateTime.now();
    LocalDateTime completedAt;
}
