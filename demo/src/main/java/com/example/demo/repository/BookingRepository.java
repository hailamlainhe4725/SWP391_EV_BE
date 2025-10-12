package com.example.demo.repository;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;

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
  List<Booking> findByUser_IdAndDeletedFalse(Long userId);

  // Lấy các booking trùng thời gian
  @Query("""
          SELECT b FROM Booking b
          WHERE b.vehicle.vehicleId = :vehicleId
            AND b.deleted = false
            AND ((b.startTime <= :endTime AND b.endTime >= :startTime))
      """)
  List<Booking> findConflictingBookings(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime);

  // Tổng số ngày user đã sử dụng trong tháng
  @Query(value = """
          SELECT COALESCE(SUM(DATEDIFF(end_time, start_time)), 0)
          FROM booking
          WHERE user_id = :userId
            AND vehicle_id = :vehicleId
            AND MONTH(start_time) = MONTH(CURDATE())
            AND YEAR(start_time) = YEAR(CURDATE())
            AND booking_status = 'COMPLETED'
            AND deleted = false
      """, nativeQuery = true)
  Double getUsedDaysThisMonth(Long userId, Long vehicleId);

  List<Booking> findByUserAndVehicleAndStartTimeAfter(User user, Vehicle vehicle, LocalDateTime now);

}
