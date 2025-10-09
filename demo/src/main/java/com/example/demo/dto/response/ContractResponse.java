package com.example.demo.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ContractResponse {
    private Long id;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String userEmail; // email của người sở hữu hợp đồng
}
