package com.example.demo.dto.response;

import java.time.LocalDateTime;

import com.example.demo.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private Long bookingId;
    private Long userId;
    private String userName;
    private Long vehicleId;
    private String vehicleModel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus bookingStatus;
}
