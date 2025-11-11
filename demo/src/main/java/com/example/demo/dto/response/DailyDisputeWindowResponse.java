package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DailyDisputeWindowResponse {
    private LocalDate date;               // Ngày trong tháng (1–30/31)
    private LocalDateTime firstCreatedAt; // Thời điểm booking đầu tiên trong ngày
    private long windowHours;             // Số giờ cửa sổ tranh chấp
    private LocalDateTime windowEndAt;    // firstCreatedAt + windowHours
}
