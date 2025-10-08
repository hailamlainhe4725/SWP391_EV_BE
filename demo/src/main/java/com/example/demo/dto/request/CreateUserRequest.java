package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role; // USER hoặc STAFF
}
