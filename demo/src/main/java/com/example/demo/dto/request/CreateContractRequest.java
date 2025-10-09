package com.example.demo.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateContractRequest {
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long userId; // Ai là người ký hợp đồng
}
