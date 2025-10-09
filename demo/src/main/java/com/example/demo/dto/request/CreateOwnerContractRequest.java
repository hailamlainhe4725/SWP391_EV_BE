package com.example.demo.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateOwnerContractRequest {
    private String ownerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long vehicleId;
    private Long userId; // chủ sở hữu
}
