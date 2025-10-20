package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import com.example.demo.enums.StaffCheckingType;

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
    StaffCheckingType checkingType;
    LocalDateTime checkTime;
    Double odometer;
    Double batteryPercent;
    Boolean damageReported;
    String notes;
    Double distanceTraveled;
    Double batteryUsedPercent;

        String userSignature;
    String staffSignature;
}
