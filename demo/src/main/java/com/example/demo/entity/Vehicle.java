package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @Column(nullable = false)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(length = 20, unique = true)
    private String licensePlate;

    private Double batteryCapacity; // dung lượng pin (kWh)
    private Double maxSpeed; // tốc độ tối đa (km/h)
    private Integer seats; // số chỗ ngồi

    @Column(length = 20)
    private String status; // Available / In Use / Maintenance

    @Column(length = 255)
    private String imageUrl; // URL ảnh duy nhất của xe

    @Column(length = 255)
    private String description; // mô tả ngắn

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
