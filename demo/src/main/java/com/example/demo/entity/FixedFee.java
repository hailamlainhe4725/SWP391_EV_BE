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
@Table(name = "fixed_fee")
public class FixedFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long fixedFeeId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    String feeType; // Maintenance, Insurance, Registration, Cleaning
    Double baseAmount;
    String frequency; // Monthly, Quarterly, Yearly
    LocalDateTime lastApplied;
    String description;

    LocalDateTime createdAt;
    boolean deleted = false;
}
