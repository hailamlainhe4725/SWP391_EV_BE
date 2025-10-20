package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.enums.BillingStatus;

import jakarta.persistence.Column;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceResponse {
    Long invoiceId;
    Long userId;
    Long vehicleId;
    BillingStatus status;
    Double totalAmount;
    LocalDateTime issuedDate;
    LocalDateTime dueDate;
    List<InvoiceDetailResponse> details;
    String fullName;
    String email;
    String phone;
        String model;
    String plateNumber;
}
