package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ContractResponse {
    private Long contractId;
    private String ownerName;
    private String vehicleName;
    private Double salePercentage;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
}
