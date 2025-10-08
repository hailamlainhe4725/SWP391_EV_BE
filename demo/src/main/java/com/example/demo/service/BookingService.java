package com.example.demo.service;

import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public Booking create(Long userId, Long vehicleId, Booking booking) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!"Available".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalStateException("Vehicle not available");
        }

        if (bookingRepository.existsByUserAndStatus(user, "Pending")) {
            throw new IllegalStateException("User already has a pending booking");
        }

        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setStatus(Booking.Status.PENDING);
        booking.setBookingDate(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public Booking updateStatus(Integer id, String status) {
        Booking b = getById(id);
        b.setStatus(Booking.Status.valueOf(status.toUpperCase()));
        return bookingRepository.save(b);
    }

    public Booking getById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    public List<Booking> getAll() {
        return bookingRepository.findAll();
    }

    public List<Booking> getByUserId(Integer userId) {
        return bookingRepository.findByUserUserId(userId);
    }

    public List<Booking> getByVehicleId(Integer vehicleId) {
        return bookingRepository.findByVehicleVehicleId(vehicleId);
    }
}