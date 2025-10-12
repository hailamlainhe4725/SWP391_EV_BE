package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import com.example.demo.enums.TransactionStatus;

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

    Double paidAmount;
    LocalDateTime paymentDate;
    String method;
    @Enumerated(EnumType.STRING)
    TransactionStatus status = TransactionStatus.INITIATED;
    @Column(name = "deleted")
    boolean deleted = false;
}
