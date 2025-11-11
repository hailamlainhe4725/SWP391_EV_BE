package com.example.demo.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.enums.CheckingStatus;
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
    CheckingStatus status; // PENDING, CONFIRMED, REJECTED
     String userComment; // ghi chú của user khi reject
        MultipartFile staffSignature;
}
