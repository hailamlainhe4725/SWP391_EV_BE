package com.example.demo.dto.request;

import lombok.Data;

@Data
public class UpdateVehicleRequest {
    private Double batteryLevel;
    private String status;
    private String imageUrl;
}
