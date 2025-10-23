package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CreateOwnerContractRequest {
    private Long contractId;
    private Long userId; // co-owner
    private Double sharePercentage;
}
