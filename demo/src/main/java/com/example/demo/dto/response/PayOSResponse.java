package com.example.demo.dto.response;

import lombok.Data;

@Data
public class PayOSResponse {
    private int code;
    private String desc;
    private DataResponse data;

    @lombok.Data
    public static class DataResponse {
        private String checkoutUrl;
        private String qrCode;
        private String orderCode;
    }
}
