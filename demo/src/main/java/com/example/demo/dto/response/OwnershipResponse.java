package com.example.demo.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnershipResponse {
    private Long ownershipId;
    private Long userId;
    private Long vehicleId;
    private String vehicleModel;
    private Double sharePercentage;
    private LocalDate purchaseDate;
}
