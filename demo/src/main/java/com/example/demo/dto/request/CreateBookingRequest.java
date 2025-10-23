package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class CreateBookingRequest {

    @NotNull
    Long vehicleId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    LocalDateTime startTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    LocalDateTime endTime;

}
