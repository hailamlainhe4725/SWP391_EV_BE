package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long userId;
    private String userName;
    private Long vehicleId;
    private String vehicleModel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
