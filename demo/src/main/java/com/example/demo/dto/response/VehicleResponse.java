package com.example.demo.dto.response;

import com.example.demo.enums.VehicleStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleResponse {
    Long vehicleId;
    String brand;
    String model;
    String plateNumber;
    String color;
    Integer year;
    Double batteryCapacityKwh;
    Integer seat;
    Double price;
    Double feeChargingPer1PercentUsed;
    Double feeOverKm;
    Double operationPerMonthPerShare;
    String description;
    String imageUrl;
    VehicleStatus status;
}
