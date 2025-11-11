package com.example.demo.dto.request;

import com.example.demo.enums.VehicleStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateVehicleRequest {
    String brand;
    String model;
    String plateNumber;
    String color;
    Integer year;
    Double batteryCapacityKwh;
    Integer seat;
    Double price;
    Double feeChargingPer1PercentUsed;
    Double feeOverKm;
    Double operationPerMonthPerShare;
    String description;
    VehicleStatus status;

    // ✅ thêm file ảnh
    MultipartFile imageFile;
}
