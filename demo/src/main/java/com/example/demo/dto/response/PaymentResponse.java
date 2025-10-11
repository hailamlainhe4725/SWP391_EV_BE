package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

import com.example.demo.enums.TransactionStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
    Long paymentId;
    Long invoiceId;
    Double amount;
    String method;
    TransactionStatus status;
    LocalDateTime paymentDate;
}
