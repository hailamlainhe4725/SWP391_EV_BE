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

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    Invoice invoice;

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
