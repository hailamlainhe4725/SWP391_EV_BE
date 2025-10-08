package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffCheckingResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleModel;
    private Double batteryLevel;
    private Double odometer;
    private String conditionNote;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}
