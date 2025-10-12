package com.example.demo.dto.request;

import lombok.Data;

import com.example.demo.enums.VariableFeeType;

@Data
public class CreateVariableFeeRequest {
    Long vehicleId;
    Long userId;
    VariableFeeType type;
    Double amount;
    String description;
}