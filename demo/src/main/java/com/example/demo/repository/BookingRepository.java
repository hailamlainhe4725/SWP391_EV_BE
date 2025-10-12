package com.example.demo.repository;

import com.example.demo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Lấy tất cả booking chưa bị xóa
    List<Booking> findByDeletedFalse();

    // Lấy tất cả booking của 1 user (chưa xóa)
    List<Booking> findByUser_UserIdAndDeletedFalse(Long userId);

    // Lấy các booking trùng thời gian
    @Query("""
                SELECT b FROM Booking b
                WHERE b.vehicle.vehicleId = :vehicleId
                  AND b.deleted = false
                  AND ((b.startTime <= :endTime AND b.endTime >= :startTime))
            """)
    List<Booking> findConflictingBookings(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

    // Tổng số ngày user đã sử dụng trong tháng
    @Query("""
                SELECT COALESCE(SUM(DATEDIFF(b.endTime, b.startTime)), 0)
                FROM Booking b
                WHERE b.user.userId = :userId
                  AND b.vehicle.vehicleId = :vehicleId
                  AND MONTH(b.startTime) = MONTH(CURRENT_DATE)
                  AND b.bookingStatus = 'COMPLETED'
                  AND b.deleted = false
            """)
    Double getUsedDaysThisMonth(Long userId, Long vehicleId);
}
