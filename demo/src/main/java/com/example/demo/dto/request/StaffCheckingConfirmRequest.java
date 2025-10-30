package com.example.demo.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class StaffCheckingConfirmRequest {
    private boolean approved;
    private String userComment;
    private MultipartFile staffSignature;

}