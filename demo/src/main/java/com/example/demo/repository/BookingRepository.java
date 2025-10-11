package com.example.demo.repository;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserAndDeletedFalse(User user);

    List<Booking> findByVehicleAndDeletedFalse(Vehicle vehicle);

    List<Booking> findByDeletedFalse();
}
