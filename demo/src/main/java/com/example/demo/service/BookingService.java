package com.example.demo.service;

import com.example.demo.dto.request.CreateBookingRequest;
import com.example.demo.dto.request.UpdateStatusBookingRequest;
import com.example.demo.dto.response.BookingResponse;
import com.example.demo.dto.response.BookingVehicleResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.BookingStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        public BookingResponse createBooking(Authentication authentication,CreateBookingRequest req) {
                User user = userRepository.findByEmail(authentication.getName())
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
                    
                if (!req.getEndTime().isAfter(req.getStartTime())) {
                        throw new RuntimeException("End time must be after start time.");
                        }
                // Giới hạn ngày sử dụng trong tháng
                Double usedDaysRaw = bookingRepository.getUsedDaysThisMonth(user.getId(), vehicle.getVehicleId());
                double usedDays = (usedDaysRaw != null) ? usedDaysRaw : 0.0;
                double allowedDays = 30 * (ownership.getTotalSharePercentage() / 100.0);
                double usageRatio = usedDays / allowedDays;

                if (usageRatio >= 1.0) {
                        throw new RuntimeException("You have reached your monthly usage limit for this vehicle.");
                }

                if (req.getStartTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Cannot create booking in the past.");
                }
                // Tính điểm ưu tiên
                double priorityScore = ownership.getTotalSharePercentage() / (1 + usedDays);


                // Tìm các booking trùng thời gian
                List<Booking> conflicts = bookingRepository.findConflictingBookings(
                                vehicle.getVehicleId(), req.getStartTime(), req.getEndTime());

                                // Nếu đã có booking được Confirmed → chặn luôn
                boolean hasConfirmed = conflicts.stream()
                        .anyMatch(b -> b.getBookingStatus() == BookingStatus.Confirmed ||b.getBookingStatus() == BookingStatus.Completed||b.getBookingStatus() == BookingStatus.InProgress );

                if (hasConfirmed) {
                throw new RuntimeException("This time slot has already been booked by another co-owner.");
                }
                                Booking booking = Booking.builder()
                                .user(user)
                                .vehicle(vehicle)
                                .startTime(req.getStartTime())
                                .endTime(req.getEndTime())
                                .bookingStatus(BookingStatus.Pending)
                                .priorityScore(priorityScore)
                                .deleted(false)
                                .createdAt(LocalDateTime.now())
                                .build();

                // Thêm booking mới vào danh sách đang cạnh tranh
                conflicts.add(booking);

                // Tìm người có priority cao nhất
                Booking topBooking = conflicts.stream()
                        .max(Comparator.comparing(Booking::getPriorityScore)
                                .thenComparing(Booking::getCreatedAt))
                        .orElse(null);

                for (Booking b : conflicts) {
                if (b.equals(topBooking)) {
                        b.setBookingStatus(BookingStatus.Pending);
                } else {
                        b.setBookingStatus(BookingStatus.Cancelled);
                }
                bookingRepository.save(b);
                }

                // Nếu booking của user hiện tại là người thắng
                if (topBooking.equals(booking)) {
                return mapToResponse(booking);
                } else {
                throw new RuntimeException("Your booking was not confirmed. A co-owner with higher priority won this slot.");
                }

        }

        // ====================== GET BOOKINGS ======================

        public List<BookingResponse> getAllBookings() {
                return bookingRepository.findAll().stream()
                                .filter(b -> !b.isDeleted())
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }
//offer them cua phu
        public List<BookingVehicleResponse> getBookingsByVehicle(Authentication authentication, Long vehicleId) {
    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return bookingRepository.findAll().stream()
            .filter(b -> b.getVehicle().getVehicleId().equals(vehicleId))
            .filter(b -> !b.isDeleted())
            .map(this::mapToResponseB)
            .collect(Collectors.toList());
}


        public List<BookingVehicleResponse> getMyBookings(Authentication authentication) {
                                User user = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                return bookingRepository.findAll().stream()
                                .filter(b -> b.getUser().getId()==user.getId())
                                .filter(b -> !b.isDeleted())
                                .map(this::mapToResponseB)
                                .collect(Collectors.toList());
        }

        public List<BookingResponse> getScheduleByVehicle( Long vehicleId){
        
        return bookingRepository.findByVehicle_VehicleIdAndBookingStatusNot(vehicleId, BookingStatus.Cancelled)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());

        }

        // ====================== UPDATE STATUS ======================
        public BookingResponse updateBookingStatus(UpdateStatusBookingRequest request) {
                Booking booking = bookingRepository.findById(request.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                try {
                        booking.setBookingStatus(Enum.valueOf(BookingStatus.class, request.getStatus()));
                } catch (Exception e) {
                        throw new RuntimeException("Invalid booking status: " + request.getStatus());
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
                                .userEmail(b.getUser().getEmail())
                                .startTime(b.getStartTime())
                                .endTime(b.getEndTime())
                                .bookingStatus(b.getBookingStatus().name())
                                .priorityScore(b.getPriorityScore())
                                .createdAt(b.getCreatedAt())
                                .build();
        }

        private BookingVehicleResponse mapToResponseB(Booking booking) {
    Vehicle v = booking.getVehicle();
    User u = booking.getUser();

    return BookingVehicleResponse.builder()
            // Vehicle info
            .vehicleId(v.getVehicleId())
            .brand(v.getBrand())
            .model(v.getModel())
            .plateNumber(v.getPlateNumber())
            .year(v.getYear())
            .imageUrl(v.getImageUrl()) // hoặc v.getVehicleImages().get(0).getUrl()

            // Booking info
            .bookingId(booking.getBookingId())
            .vehicleName(v.getModel() + " - " + v.getBrand())
            .userName(u.getFullName())
            .bookingStatus(booking.getBookingStatus().name())
            .priorityScore(booking.getPriorityScore())
            .startTime(booking.getStartTime())
            .endTime(booking.getEndTime())
            .createdAt(booking.getCreatedAt())
            .build();
}

}
