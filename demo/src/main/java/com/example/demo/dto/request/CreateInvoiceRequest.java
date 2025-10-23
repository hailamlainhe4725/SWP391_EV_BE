package com.example.demo.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.demo.dto.response.InvoiceResponse.InvoiceResponseBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CreateInvoiceRequest {
    private String email;
}
