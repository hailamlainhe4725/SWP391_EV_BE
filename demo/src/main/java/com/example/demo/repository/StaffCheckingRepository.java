package com.example.demo.repository;

import com.example.demo.entity.StaffChecking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffCheckingRepository extends JpaRepository<StaffChecking, Integer> {
    List<StaffChecking> findByBookingBookingId(Long bookingId);

    List<StaffChecking> findByVehicleVehicleId(Long vehicleId);
}