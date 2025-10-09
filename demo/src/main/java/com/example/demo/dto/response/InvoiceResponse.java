package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

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
    String status;
    Double totalAmount;
    LocalDateTime issuedDate;
    LocalDateTime dueDate;
    List<InvoiceDetailResponse> details;
}
