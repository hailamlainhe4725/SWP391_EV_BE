package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {
    private Long vehicleId;
    private String licensePlate;
    private String model;
    private String brand;
    private Double batteryLevel;
    private String status;
    private String imageUrl;
}
