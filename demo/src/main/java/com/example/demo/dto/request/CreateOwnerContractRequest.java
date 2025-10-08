package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CreateOwnerContractRequest {
    private Long ownerId;
    private Long contractId;
    private String roleInContract;
}
