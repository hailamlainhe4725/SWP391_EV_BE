package com.example.demo.dto.request;

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
    Long userId;
    Long staffId;
    Long bookingId;
    String checkType; // CheckIn, CheckOut
    Double odometer;
    Double batteryPercent;
    Boolean damageReported;
    String notes;
}
