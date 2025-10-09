package com.example.demo.dto.response;

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
    Double batteryCapacityKWh;
    Double operatingCostPerDay;
    Double operatingCostPerKm;
    String description;
    String imageUrl;
    String status;
}
