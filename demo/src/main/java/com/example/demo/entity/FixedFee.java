package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

import com.example.demo.enums.FixFeeType;

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

    @Enumerated(EnumType.STRING)
    FixFeeType type;
    Double baseAmount;
    String frequency; // Monthly, Quarterly, Yearly
    LocalDateTime lastApplied;
    String description;

    LocalDateTime createdAt;
    boolean deleted = false;
}
