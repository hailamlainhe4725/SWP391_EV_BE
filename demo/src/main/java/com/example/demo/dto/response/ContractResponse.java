package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;

@Data
@Builder
public class ContractResponse {
    private Long contractId;
    private User user;
    private Vehicle vehicle;
    private User admin;
    private String userSignature;
    private String adminSignature;
    private Double salePercentage;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
        private Double insurance;
    private Double registration;
    private Double maintenance;
    private Double cleaning;
    private Double operationPerMonth;
}
