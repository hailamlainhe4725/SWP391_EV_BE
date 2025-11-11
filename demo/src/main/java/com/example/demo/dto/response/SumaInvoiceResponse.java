package com.example.demo.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumaInvoiceResponse {
    private Long sumaInvoiceId;
    private String userName;
    private String month;
    private Double totalAmount;
    private String status;
    private List<InvoiceResponse> invoices;
}
