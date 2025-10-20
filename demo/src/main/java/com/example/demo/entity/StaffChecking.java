package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import com.example.demo.enums.CheckingStatus;
import com.example.demo.enums.StaffCheckingType;

@Entity
@Table(name = "staff_checking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffChecking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long checkingId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    User staff;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    StaffCheckingType type; // CHECK_IN, CHECK_OUT

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime checkTime = LocalDateTime.now();

    Double odometer; // số km thực tế
    Double batteryPercent; // phần trăm pin
    Boolean damageReported; // xe có bị hư hại?
    String notes; // ghi chú staff

    Double distanceTraveled; // hệ thống tính toán (CheckIn - CheckOut)
    Double batteryUsedPercent; // hệ thống tính toán

    @Builder.Default
    @Column(name = "deleted")
    boolean deleted = false;

    @Column(nullable = false)
@Enumerated(EnumType.STRING)
private CheckingStatus status; // PENDING, CONFIRMED, REJECTED

private String userComment; // ghi chú của user khi reject

}
