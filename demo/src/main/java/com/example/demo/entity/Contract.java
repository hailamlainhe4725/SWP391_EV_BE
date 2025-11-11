package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.enums.ContractStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contract")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long contractId;

    // Xe được bán cổ phần
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    // Chủ xe (người bán cổ phần)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;
         @ManyToOne
        @JoinColumn(name = "admin_id", nullable = false)
    User admin;

    LocalDate startDate;
    LocalDate endDate;

    // Tỷ lệ cổ phần muốn bán (VD: 40%)
    Double salePercentage;

    @Enumerated(EnumType.STRING)
    ContractStatus status = ContractStatus.PENDING;

    LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    @Builder.Default
    Boolean deleted = false;
     String adminSignatureUrl;
 String userSignatureUrl;

    Double registration;
    Double maintenance;
    Double insurance;
    Double cleaning;
    Double operationPerMonth;
}