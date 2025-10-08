package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CreateInvoiceRequest {
    private Long userId;
    private Long vehicleId;
    private Double totalAmount;
    private String description;
}
