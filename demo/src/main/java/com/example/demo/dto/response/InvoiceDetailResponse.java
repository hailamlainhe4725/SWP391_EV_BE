package com.example.demo.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceDetailResponse {
 Long detailId;
    String feeType;       // loại phí (ví dụ: Maintenance, Insurance...)
    String sourceType;    // "Fixed" hoặc "Variable"
    Long relatedId;       // id của fee gốc (FixedFeeId hoặc VariableFeeId)
    String description;   // mô tả chi tiết
    Double amount;        // số tiền
    LocalDateTime createdAt; // ngày tạo
}
