package com.example.demo.repository;

import com.example.demo.entity.Booking;
import com.example.demo.entity.StaffChecking;
import com.example.demo.enums.StaffCheckingType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffCheckingRepository extends JpaRepository<StaffChecking, Long> {
    List<StaffChecking> findByBookingBookingId(Long bookingId);

    List<StaffChecking> findByDeletedFalse();

    List<StaffChecking> findByBookingAndDeletedFalse(Booking booking);

    List<StaffChecking> findByVehicleVehicleId(Long vehicleId);

    List<StaffChecking> findByUser_IdAndDeletedFalse(Long Id);

    Optional<StaffChecking> findByBooking_BookingIdAndTypeAndDeletedFalse(Long bookingId, StaffCheckingType type);
}