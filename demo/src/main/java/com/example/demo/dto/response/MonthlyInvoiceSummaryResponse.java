package com.example.demo.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyInvoiceSummaryResponse {
    private String userName;
    private String month; // "2025-10"
    private Double totalAmount;
    private List<InvoiceResponse> invoices;
}