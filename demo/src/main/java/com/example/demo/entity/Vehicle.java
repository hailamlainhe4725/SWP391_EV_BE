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
    Integer seat;
    Double price;

    // ❌ KHÔNG nên khởi tạo trực tiếp với phép toán dùng biến khác
    Double feeChargingPer1PercentUsed;
    Double feeOverKm = 3000.0;
    Double operationPerMonthPerShare;

    String description;
    String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    VehicleStatus status = VehicleStatus.Available;

    @Builder.Default
    @Column(name = "deleted")
    boolean deleted = false;

    // ✅ Viết lại logic tính toán an toàn
    public void calculateFees() {
        
            this.feeChargingPer1PercentUsed = 2500 * batteryCapacityKwh * 0.01;
        
            this.operationPerMonthPerShare = price * 0.01 * 0.1;
    }}
