package com.example.demo.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CreateContractRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalValue;
}
