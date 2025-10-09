package com.example.demo.repository;

import com.example.demo.entity.Booking;
import com.example.demo.entity.StaffChecking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffCheckingRepository extends JpaRepository<StaffChecking, Long> {
    List<StaffChecking> findByBookingBookingId(Long bookingId);

    List<StaffChecking> findByDeletedFalse();

    List<StaffChecking> findByBookingAndDeletedFalse(Booking booking);

    List<StaffChecking> findByVehicleVehicleId(Long vehicleId);
}