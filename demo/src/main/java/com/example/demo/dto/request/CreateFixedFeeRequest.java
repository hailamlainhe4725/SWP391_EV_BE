package com.example.demo.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateFixedFeeRequest {
    private Long vehicleId;
    private String feeType; // Maintenance, Insurance, Registration, Cleaning
    private BigDecimal baseAmount;
    private String frequency; // Monthly, Quarterly, Yearly
    private LocalDate lastApplied;
    private String description;
}