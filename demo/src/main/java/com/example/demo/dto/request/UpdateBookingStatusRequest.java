package com.example.demo.dto.request;

import lombok.Data;

@Data
public class UpdateBookingStatusRequest {
    private String status; // Pending, Approved, Rejected
}
