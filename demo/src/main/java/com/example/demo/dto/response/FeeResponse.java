package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeResponse {
    private Long id;
    private String name;
    private Double amount;
    private String type; // Fixed / Variable
}
