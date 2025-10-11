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
}
