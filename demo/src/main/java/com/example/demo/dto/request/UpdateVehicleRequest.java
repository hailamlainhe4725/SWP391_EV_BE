package com.example.demo.dto.request;

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
    Double operatingCostPerDay;
    Double operatingCostPerKm;
    String description;
    String status;
}
