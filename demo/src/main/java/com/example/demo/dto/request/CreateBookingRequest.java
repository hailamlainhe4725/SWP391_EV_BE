package com.example.demo.dto.request;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreateBookingRequest {
    private Long userId;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
