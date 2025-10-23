package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwnershipResponse {
    private Long ownershipId;
    private String userName;
    private String vehicleName;
    private Double totalSharePercentage;
    private String status;
    private LocalDateTime createdAt;
    Double allowedKmThisMonth; // Số km được phép trong tháng (theo tỷ lệ cổ phần)
    Double usedKmThisMonth; // Số km đã sử dụng trong tháng
    Double allowedDaysThisMonth; // Số ngày được phép sử dụng trong tháng
    Double usedDaysThisMonth; // Số ngày đã dùng trong tháng
}
