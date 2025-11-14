package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;

@Data
@Builder
public class OwnerContractResponse {
    private Long ownerContractId;
    private User user;
    private User admin;
    private Double sharePercentage;
    private Vehicle vehicle;
    private String contractStatus;
    private String ownerContractStatus;
    private LocalDateTime createdAt;
        private String userSignature;
    private String adminSignature;
    private Long contractId;
            private Double insurance;
    private Double registration;
    private Double maintenance;
    private Double cleaning;
    private Double operationPerMonth;
}
