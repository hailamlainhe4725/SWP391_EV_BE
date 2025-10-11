package com.example.demo.service;

import com.example.demo.dto.request.CreateBookingRequest;
import com.example.demo.dto.request.UpdateBookingStatusRequest;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.enums.BookingStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

        private final BookingRepository bookingRepository;
        private final UserRepository userRepository;
        private final VehicleRepository vehicleRepository;

        // === User: get all bookings by email
        public List<BookingResponse> getByUserEmail(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                return bookingRepository.findByUserAndDeletedFalse(user)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // === Staff: get all bookings
        public List<BookingResponse> getAll() {
                return bookingRepository.findByDeletedFalse().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // === User: create booking
        public BookingResponse create(CreateBookingRequest req, String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

                if (req.getEndTime().isBefore(req.getStartTime())) {
                        throw new IllegalArgumentException("End time must be after start time");
                }

                Booking booking = Booking.builder()
                                .user(user)
                                .vehicle(vehicle)
                                .startTime(req.getStartTime())
                                .endTime(req.getEndTime())
                                .bookingStatus(BookingStatus.Pending)
                                .deleted(false)
                                .build();

                bookingRepository.save(booking);
                return mapToResponse(booking);
        }

        // === Staff: update status (Confirm/Complete/Cancel)
        public BookingResponse updateStatus(Long bookingId, UpdateBookingStatusRequest req) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                booking.setBookingStatus(req.getBookingStatus());
                bookingRepository.save(booking);
                return mapToResponse(booking);
        }

        // === Soft delete (staff)
        public void softDelete(Long id) {
                Booking booking = bookingRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                booking.setDeleted(true);
                bookingRepository.save(booking);
        }

        // === Mapper
        private BookingResponse mapToResponse(Booking b) {
                return BookingResponse.builder()
                                .bookingId(b.getBookingId())
                                .userId(b.getUser().getId())
                                .userName(b.getUser().getFullName())
                                .vehicleId(b.getVehicle().getVehicleId())
                                .vehicleModel(b.getVehicle().getModel())
                                .startTime(b.getStartTime())
                                .endTime(b.getEndTime())
                                .bookingStatus(b.getBookingStatus())
                                .build();
        }
}
