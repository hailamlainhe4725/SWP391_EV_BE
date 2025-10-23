package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

import com.example.demo.enums.VariableFeeType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "variable_fee")
public class VariableFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long variableFeeId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "checking_id")
    StaffChecking staffChecking;

    @Enumerated(EnumType.STRING)
    VariableFeeType type;
    Double amount;
    String description;

    @ManyToOne
    @JoinColumn(name = "recorded_by")
    User recordedBy;

    LocalDateTime createdAt;
    @Column(name = "deleted")
    boolean deleted = false;
}
