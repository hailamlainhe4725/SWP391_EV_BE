package com.example.demo.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateContractRequest {
    private Long vehicleId;
    private Long userId; // chủ xe (owner)
    private Double salePercentage;
    private LocalDate startDate;
    private LocalDate endDate;
}
