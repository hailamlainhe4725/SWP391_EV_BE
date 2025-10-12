package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    Long bookingId;
    Long vehicleId;
    String vehicleName;
    String userName;
    String bookingStatus;
    Double priorityScore;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime createdAt;
}
