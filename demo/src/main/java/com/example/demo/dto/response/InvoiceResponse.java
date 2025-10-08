package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse {
    private Long invoiceId;
    private Long userId;
    private Long vehicleId;
    private Double totalAmount;
    private String description;
    private LocalDateTime createdAt;
}
