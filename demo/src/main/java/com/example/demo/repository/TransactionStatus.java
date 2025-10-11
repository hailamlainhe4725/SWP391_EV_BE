package com.example.demo.repository;

public enum TransactionStatus {
    INITIATED, // Giao dịch mới tạo, đang chờ kết quả
    SUCCESS, // Thanh toán thành công
    FAILED // Thanh toán thất bại
}
