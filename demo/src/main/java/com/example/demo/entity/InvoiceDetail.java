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
@Table(name = "invoice_detail")
public class InvoiceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long detailId;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    Invoice invoice;

    String feeType;
    String sourceType;
    Long relatedId;
    String description;
    Double amount;
    LocalDateTime createdAt;

    @Column(name = "deleted")
    boolean deleted = false;
}
