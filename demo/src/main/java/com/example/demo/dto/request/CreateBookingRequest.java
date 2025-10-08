package com.example.demo.dto.request;

@Data
public class CreateBookingRequest {
    private Long userId;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
