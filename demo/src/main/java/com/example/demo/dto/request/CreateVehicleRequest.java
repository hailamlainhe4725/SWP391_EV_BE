package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CreateVehicleRequest {
    private String licensePlate;
    private String model;
    private String brand;
    private Double batteryLevel;
    private String status; // Available / Maintenance / InUse
    private String imageUrl;
}
