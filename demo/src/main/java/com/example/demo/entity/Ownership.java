package com.example.demo.entity;

import com.example.demo.enums.OwnershipStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ownership")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ownership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long ownershipId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    Double totalSharePercentage = 0.0;

    @Enumerated(EnumType.STRING)
    OwnershipStatus status = OwnershipStatus.ACTIVE;

    LocalDateTime createdAt = LocalDateTime.now();

    // ===== Tracking usage =====
    Double allowedKmThisMonth; // Số km được phép trong tháng (theo tỷ lệ cổ phần)
    Double usedKmThisMonth; // Số km đã sử dụng trong tháng
    Double allowedDaysThisMonth; // Số ngày được phép sử dụng trong tháng
    Double usedDaysThisMonth; // Số ngày đã dùng trong tháng

    @Builder.Default
    @Column(name = "deleted")
    boolean deleted = false;

    // === Getter tiện ích ===
    public boolean isOverKmLimit() {
        return usedKmThisMonth != null && allowedKmThisMonth != null && usedKmThisMonth >= allowedKmThisMonth;
    }

    public boolean isOverDayLimit() {
        return usedDaysThisMonth != null && allowedDaysThisMonth != null && usedDaysThisMonth >= allowedDaysThisMonth;
    }

    // === Tính lại hạn mức mỗi khi bắt đầu tháng mới ===
    public void resetMonthlyLimit(double baseKmLimit) {
        this.allowedKmThisMonth = baseKmLimit * (totalSharePercentage / 100.0);
        this.allowedDaysThisMonth = 30 * (totalSharePercentage / 100.0);
        this.usedKmThisMonth = 0.0;
        this.usedDaysThisMonth = 0.0;
    }

    // === Tăng số km và ngày đã sử dụng ===
    public void addUsedKm(double km) {
        if (usedKmThisMonth == null)
            usedKmThisMonth = 0.0;
        usedKmThisMonth += km;
    }

    public void addUsedDay(double days) {
        if (usedDaysThisMonth == null)
            usedDaysThisMonth = 0.0;
        usedDaysThisMonth += days;
    }

    @Column(name = "last_reset_month")
    private Integer lastResetMonth;

}
