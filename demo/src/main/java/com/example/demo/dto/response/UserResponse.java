package com.example.demo.dto.response;

import com.example.demo.enums.VerifyStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;

    String cccd;
    String gplx;
    String cccdImagePath;
    String gplxImagePath;
    Boolean verified;
    VerifyStatus verifyStatus;
    String verifyNote;
}
