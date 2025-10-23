package com.example.demo.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingVehicleResponse {
    // Vehicle info
    private Long vehicleId;
    private String brand;
    private String model;
    private String plateNumber;
    private Integer year;
    private String imageUrl;

    // Booking info
    private Long bookingId;
    private String vehicleName;
    private String userName;
    private String bookingStatus;
    private Double priorityScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
}
