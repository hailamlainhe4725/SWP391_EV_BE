package com.example.demo.entity;

import com.example.demo.enums.VehicleStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long vehicleId;

    String brand;
    String model;
    String plateNumber;
    String color;
    Integer year;
    Double batteryCapacityKwh;
    Double operatingCostPerDay;
    Double operatingCostPerKm;
    String description;
    String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    VehicleStatus status = VehicleStatus.Available; // AVAILABLE, RENTED, MAINTENANCE

    @Builder.Default
    @Column(name = "deleted")
    boolean deleted = false;
}
