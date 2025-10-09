package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwnershipResponse {
    Long ownershipId;
    Long userId;
    String userName;
    Long vehicleId;
    String vehicleModel;
    Double totalSharePercentage;
    String status;
}
