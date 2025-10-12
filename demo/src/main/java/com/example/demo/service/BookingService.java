package com.example.demo.service;

import com.example.demo.dto.request.CreateBookingRequest;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.BookingStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

        private final BookingRepository bookingRepository;
        private final VehicleRepository vehicleRepository;
        private final UserRepository userRepository;
        private final OwnershipRepository ownershipRepository;

        // ====================== CREATE BOOKING ======================
        public BookingResponse createBooking(CreateBookingRequest req) {
                User user = userRepository.findById(req.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

                Ownership ownership = ownershipRepository
                                .findByUser_IdAndVehicle_VehicleId(user.getId(), vehicle.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "You are not a co-owner of this vehicle"));

                // Kiểm tra giới hạn km
                if (ownership.isOverKmLimit()) {
                        throw new RuntimeException(
                                        "You have exceeded your monthly km limit for this vehicle. Booking blocked.");
                }
                // Giới hạn ngày sử dụng trong tháng
                double usedDays = bookingRepository.getUsedDaysThisMonth(user.getId(), vehicle.getVehicleId());
                double allowedDays = 30 * (ownership.getTotalSharePercentage() / 100.0);
                double usageRatio = usedDays / allowedDays;

                if (usageRatio >= 1.0) {
                        throw new RuntimeException("You have reached your monthly usage limit for this vehicle.");
                }

                // Đặt thời gian cố định (1AM - 11PM)
                LocalDateTime bookingDate = req.getStartTime().toLocalDate().atStartOfDay();
                LocalDateTime startTime = bookingDate.plusHours(1); // 01:00 AM
                LocalDateTime endTime = bookingDate.plusHours(23); // 11:00 PM

                // Tính điểm ưu tiên
                double priorityScore = ownership.getTotalSharePercentage() / (1 + usedDays);

                Booking booking = Booking.builder()
                                .user(user)
                                .vehicle(vehicle)
                                .startTime(startTime)
                                .endTime(endTime)
                                .bookingStatus(BookingStatus.Pending)
                                .priorityScore(priorityScore)
                                .deleted(false)
                                .build();

                // Tìm các booking trùng thời gian
                List<Booking> conflicts = bookingRepository.findConflictingBookings(
                                vehicle.getVehicleId(), startTime, endTime);

                // Nếu không có ai khác → auto confirm
                if (conflicts.isEmpty()) {
                        booking.setBookingStatus(BookingStatus.Confirmed);
                } else {
                        // Lấy booking có priority cao nhất trong nhóm
                        Booking topBooking = conflicts.stream()
                                        .max(Comparator.comparing(Booking::getPriorityScore)
                                                        .thenComparing(Booking::getCreatedAt))
                                        .orElse(null);

                        // So sánh người mới
                        if (topBooking != null && priorityScore > topBooking.getPriorityScore()) {
                                // New booking thắng → confirm
                                booking.setBookingStatus(BookingStatus.Confirmed);

                                // Hủy những người cũ
                                for (Booking b : conflicts) {
                                        b.setBookingStatus(BookingStatus.Cancelled);
                                        bookingRepository.save(b);
                                }
                        } else {
                                booking.setBookingStatus(BookingStatus.Cancelled);
                                throw new RuntimeException("A co-owner with higher priority already booked this slot.");
                        }
                }

                bookingRepository.save(booking);
                return mapToResponse(booking);
        }

        // ====================== GET BOOKINGS ======================
        public List<BookingResponse> getUserBookings(Long userId) {
                return bookingRepository.findAll().stream()
                                .filter(b -> b.getUser().getId().equals(userId))
                                .filter(b -> !b.isDeleted())
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<BookingResponse> getAllBookings() {
                return bookingRepository.findAll().stream()
                                .filter(b -> !b.isDeleted())
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<BookingResponse> getBookingsByEmail(String email) {
                return bookingRepository.findAll().stream()
                                .filter(b -> b.getUser().getEmail().equals(email))
                                .filter(b -> !b.isDeleted())
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // ====================== UPDATE STATUS ======================
        public BookingResponse updateBookingStatus(Long id, String status) {
                Booking booking = bookingRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                try {
                        booking.setBookingStatus(Enum.valueOf(BookingStatus.class, status.toUpperCase()));
                } catch (Exception e) {
                        throw new RuntimeException("Invalid booking status: " + status);
                }

                bookingRepository.save(booking);
                return mapToResponse(booking);
        }

        // ====================== CANCEL BOOKING ======================
        public void cancelBooking(Long bookingId) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                if (booking.getBookingStatus() == BookingStatus.Confirmed
                                || booking.getBookingStatus() == BookingStatus.Pending) {
                        booking.setBookingStatus(BookingStatus.Cancelled);
                        bookingRepository.save(booking);
                } else {
                        throw new RuntimeException("Booking cannot be cancelled in its current state");
                }
        }

        // ====================== SOFT DELETE ======================
        public void softDeleteBooking(Long bookingId) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                booking.setDeleted(true);
                bookingRepository.save(booking);
        }

        // ====================== MAPPING ======================
        private BookingResponse mapToResponse(Booking b) {
                return BookingResponse.builder()
                                .bookingId(b.getBookingId())
                                .vehicleId(b.getVehicle().getVehicleId())
                                .vehicleName(b.getVehicle().getBrand() + " " + b.getVehicle().getModel())
                                .userName(b.getUser().getFullName())
                                .startTime(b.getStartTime())
                                .endTime(b.getEndTime())
                                .bookingStatus(b.getBookingStatus().name())
                                .priorityScore(b.getPriorityScore())
                                .createdAt(b.getCreatedAt())
                                .build();
        }
}
