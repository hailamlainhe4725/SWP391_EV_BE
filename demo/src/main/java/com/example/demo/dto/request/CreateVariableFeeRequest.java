package com.example.demo.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateVariableFeeRequest {
    private Long checkingId;
    private Long bookingId;
    private Long vehicleId;
    private Long userId;
    private String feeType; // Charging, Overused, OverOdometer, Damage, Upgrade
    private BigDecimal amount;
    private String description;
    private Long recordedBy;
    private LocalDateTime createdAt;
}