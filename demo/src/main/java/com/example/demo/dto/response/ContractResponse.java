package com.example.demo.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponse {
    private Long contractId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalValue;
}
