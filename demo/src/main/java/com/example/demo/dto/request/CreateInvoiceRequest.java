package com.example.demo.dto.request;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class CreateInvoiceRequest {
    private Long userId;
    private Long vehicleId;
    private String note;
}
