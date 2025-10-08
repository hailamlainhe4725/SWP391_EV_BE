package com.example.demo.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CreateOwnershipRequest {
    private Long userId;
    private Long vehicleId;
    private Double sharePercentage;
    private Double purchasePrice;
    private LocalDate purchaseDate;
}
