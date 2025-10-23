package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OwnerContractResponse {
    private Long ownerContractId;
    private String userName;
    private Double sharePercentage;
    private String contractStatus;
    private LocalDateTime createdAt;
}
