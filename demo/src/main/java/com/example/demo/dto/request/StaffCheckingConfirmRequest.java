package com.example.demo.dto.request;

import lombok.Data;

@Data
public class StaffCheckingConfirmRequest {
    private boolean approved;
    private String userComment;
}