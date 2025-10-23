package com.example.demo.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceDetailDto {
    private String feeType;
    private String sourceType; // Variable or Fixed
    private Long relatedId;
    private String description;
    private BigDecimal amount;
}
