package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {
    @NotNull
    Long userId;
    @NotNull
    Long vehicleId;
    @NotNull
    LocalDateTime startTime;

}
