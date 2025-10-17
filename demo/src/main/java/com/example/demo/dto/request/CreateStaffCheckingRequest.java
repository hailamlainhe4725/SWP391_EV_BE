package com.example.demo.dto.request;

import com.example.demo.enums.StaffCheckingType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateStaffCheckingRequest {
    Long vehicleId;
    String userEmail;
    Long bookingId;
    StaffCheckingType staffCheckingType;
    Double odometer;
    Double batteryPercent;
    Boolean damageReported;
    String notes;
}
