package com.example.demo.dto.request;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class CreateInvoiceRequest {
    private Long userId;
    private Long vehicleId;
    private List<InvoiceDetailDto> details; // nested helper class or separate DTO
    private String note;
    private LocalDateTime issuedDate;
}
