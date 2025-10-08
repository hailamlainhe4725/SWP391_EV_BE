package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Long invoiceId;
    private Double amount;
    private String method; // Card, Cash, Transfer
}
