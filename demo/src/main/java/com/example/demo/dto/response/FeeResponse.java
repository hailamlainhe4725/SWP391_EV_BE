package com.example.demo.dto.response;

import com.example.demo.enums.FixFeeType;
import com.example.demo.enums.VariableFeeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeeResponse {
    Long feeId;
    String sourceType; // "Variable" or "Fixed"

    VariableFeeType variableFeeType;
    FixFeeType fixedFeeType;

    Long vehicleId;
    Long userId;
    Double amount;
    String description;
    LocalDateTime createdAt;
}
