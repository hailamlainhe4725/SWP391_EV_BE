package com.example.demo.dto.request;

import lombok.Data;
import java.time.LocalDate;

import com.example.demo.enums.ContractStatus;

@Data
public class CreateContractRequest {
    private Long vehicleId;
    private Long userId; // chủ xe (owner)
    private Double salePercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;
}
