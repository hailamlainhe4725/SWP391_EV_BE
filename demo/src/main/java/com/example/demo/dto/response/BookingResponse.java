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
    String userEmail;
    String bookingStatus;
    Double priorityScore;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime createdAt;
      private boolean disputed;         // Có tranh chấp hay không
    private Boolean disputeWinner;    // true = thắng, false = thua, null = chưa tranh chấp
}
