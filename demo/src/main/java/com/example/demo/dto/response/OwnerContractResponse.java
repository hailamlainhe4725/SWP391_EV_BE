package com.example.demo.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OwnerContractResponse {
    private Long id;
    private String ownerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long vehicleId;
    private String userEmail;
}
