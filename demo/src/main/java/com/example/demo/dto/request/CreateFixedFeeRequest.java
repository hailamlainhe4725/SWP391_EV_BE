package com.example.demo.dto.request;

import lombok.Data;

import com.example.demo.enums.FixFeeType;

@Data
public class CreateFixedFeeRequest {
    Long vehicleId;
    FixFeeType type;
    Double baseAmount;
    String frequency;
    String description;
}