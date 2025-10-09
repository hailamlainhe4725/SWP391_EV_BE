package com.example.demo.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOwnershipRequest {
    Long userId;
    Long vehicleId;
    Double totalSharePercentage;
    String status; // Active / Inactive

}
