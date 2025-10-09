package com.example.demo.dto.response;

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
    String feeType;
    String sourceType; // "Variable" or "Fixed"
    Long vehicleId;
    Long userId;
    Double amount;
    String description;
    LocalDateTime createdAt;
}
