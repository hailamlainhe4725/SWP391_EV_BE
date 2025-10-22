package com.example.demo.repository;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.enums.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

@Query(value = """
    SELECT 
        SUM(
            DATEDIFF(
                LEAST(
                   
                    b.end_time,
                    LAST_DAY(CURDATE())
                ),
                GREATEST(
                   
                    b.start_time,
                    DATE_FORMAT(CURDATE(), '%Y-%m-01')
                )
            ) + 1
        ) AS UsedDays
    FROM booking b
    WHERE b.user_id = :userId
      AND b.vehicle_id = :vehicleId
      AND b.booking_status IN ('Confirmed', 'Completed')
      AND b.deleted = 0
      
      AND b.end_time >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
      AND b.start_time <= LAST_DAY(CURDATE())
""", nativeQuery = true)
Double getUsedDaysThisMonth(Long userId, Long vehicleId);



  List<Booking> findByUserAndVehicleAndStartTimeAfter(User user, Vehicle vehicle, LocalDateTime now);

  List<Booking> findByVehicle_VehicleIdAndBookingStatusNot(Long vehicleId, BookingStatus cancelled);

}
