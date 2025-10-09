package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffCheckingResponse {
    Long checkingId;
    Long vehicleId;
    String vehicleModel;
    Long userId;
    String userName;
    Long staffId;
    String staffName;
    Long bookingId;
    String checkType;
    LocalDateTime checkTime;
    Double odometer;
    Double batteryPercent;
    Boolean damageReported;
    String notes;
    Double distanceTraveled;
    Double batteryUsedPercent;
}
