package com.example.demo.service;

import com.example.demo.entity.Booking;
import com.example.demo.entity.StaffChecking;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.StaffCheckingRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffCheckingService {
    private final StaffCheckingRepository staffCheckingRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public StaffChecking createCheckOut(Integer bookingId, Long staffId,
            StaffChecking sc) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        sc.setBooking(booking);
        sc.setStaff(staff);
        sc.setCheckType(StaffChecking.Type.CHECKOUT);
        sc.setCheckTime(LocalDateTime.now());
        sc.setStatus("pending");
        return staffCheckingRepository.save(sc);
    }

    public StaffChecking createCheckIn(Integer bookingId, Long staffId,
            StaffChecking sc) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        sc.setBooking(booking);
        sc.setStaff(staff);
        sc.setCheckType(StaffChecking.Type.CHECKIN);
        sc.setCheckTime(LocalDateTime.now());
        sc.setStatus("pending");
        return staffCheckingRepository.save(sc);
    }

    public StaffChecking getById(Integer id) {
        return staffCheckingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StaffChecking not found"));
    }

    public List<StaffChecking> getByBooking(Long bookingId) {
        return staffCheckingRepository.findByBookingBookingId(bookingId);
    }

    public List<StaffChecking> getAllChecks() {
        return staffCheckingRepository.findAll();
    }

    public List<StaffChecking> getByVehicle(Long vehicleId) {
        return staffCheckingRepository.findByVehicleVehicleId(vehicleId);
    }

    public List<StaffChecking> getById(Long id) {
        return staffCheckingRepository.findByBookingBookingId(id);
    }

    public StaffChecking save(StaffChecking check) {
        return staffCheckingRepository.save(check);
    }

    public void delete(Long id) {
        staffCheckingRepository.deleteById(id.intValue());
    }

}
