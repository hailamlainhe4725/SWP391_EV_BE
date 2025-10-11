package com.example.demo.repository;

public enum BillingStatus {
    OPEN, // Hóa đơn mới tạo hoặc chưa thanh toán thành công
    SETTLED, // Đã thanh toán thành công (đủ tiền)
    OVERDUE // Quá hạn thanh toán
}
