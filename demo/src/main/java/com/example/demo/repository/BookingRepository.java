package com.example.demo.repository;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserUserId(Integer userId);

    boolean existsByUserAndStatus(User user, String status);

    List<Booking> findByVehicleVehicleId(Integer vehicleId);
}
