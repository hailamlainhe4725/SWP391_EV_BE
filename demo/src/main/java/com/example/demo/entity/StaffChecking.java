package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffChecking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkingId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    private double batteryLevel;
    private double odometer;
    private String damageReport;
    private LocalDateTime checkTime;
    private Booking booking;
    @Enumerated(EnumType.STRING)
    private Type checkType;

    public enum Type {
        CHECKIN, CHECKOUT
    }

    private String status;
}