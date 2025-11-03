package com.example.demo.entity;

import com.example.demo.enums.OwnerContractStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "owner_contract")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwnerContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ownerContractId;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    Contract contract;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user; // người mua cổ phần (co-owner)
     @ManyToOne
        @JoinColumn(name = "admin_id", nullable = false)
    User admin; // người mua cổ phần (co-owner)
    // % cổ phần mua (VD: 20%)
    Double sharePercentage;

    @ManyToOne
    @JoinColumn(name = "vehicleId", nullable = false)
    Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    OwnerContractStatus status = OwnerContractStatus.ACTIVE;

    LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    @Column(nullable = false)
    Boolean deleted = false;

         String adminSignatureUrl;
     String userSignatureUrl;
}