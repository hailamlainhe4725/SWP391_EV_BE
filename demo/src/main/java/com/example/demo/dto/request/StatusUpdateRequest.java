package com.example.demo.dto.request;

import com.example.demo.enums.ContractStatus;

import lombok.Data;

@Data
public class StatusUpdateRequest {
    private ContractStatus status;
}
