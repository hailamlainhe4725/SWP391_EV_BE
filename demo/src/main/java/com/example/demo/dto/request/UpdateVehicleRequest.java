package com.example.demo.dto.request;

import com.example.demo.enums.VehicleStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateVehicleRequest {
    String brand;
    String model;
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
