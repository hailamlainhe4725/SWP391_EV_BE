package com.example.demo.dto.response;

import com.example.demo.enums.OwnershipStatus;
import com.example.demo.enums.VehicleStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwnershipVehicleResponse {

    // === Ownership info ===
    Long ownershipId;
    Double totalSharePercentage;
    OwnershipStatus status;
    LocalDateTime createdAt;
    Double allowedKmThisMonth;
    Double usedKmThisMonth;
    Double allowedDaysThisMonth;
    Double usedDaysThisMonth;

    // === Vehicle info ===
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
    VehicleStatus vehicleStatus;
}
