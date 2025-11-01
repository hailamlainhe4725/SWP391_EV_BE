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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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

        public BookingResponse createBooking(Authentication authentication, CreateBookingRequest req) {
        // --- 1️⃣ Xác thực user và ownership ---
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Ownership ownership = ownershipRepository
                .findByUser_IdAndVehicle_VehicleId(user.getId(), vehicle.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not a co-owner of this vehicle"));

        // --- 2️⃣ Validate thời gian hợp lệ ---
        if (!req.getEndTime().isAfter(req.getStartTime())) {
                throw new RuntimeException("End time must be after start time.");
        }
        if (req.getStartTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Cannot create booking in the past.");
        }

        // --- 3️⃣ Kiểm tra giới hạn km ---
        if (ownership.isOverKmLimit()) {
                throw new RuntimeException("You have exceeded your monthly km limit for this vehicle. Booking blocked.");
        }

        // --- 4️⃣ Kiểm tra tỷ lệ sử dụng trong tháng (bao gồm booking sắp đặt) ---
        Double usedDaysRaw = bookingRepository.getUsedDaysThisMonth(user.getId(), vehicle.getVehicleId());
        double usedDays = (usedDaysRaw != null) ? usedDaysRaw : 0.0;

        long bookingDays = ChronoUnit.DAYS.between(
                req.getStartTime().toLocalDate(),
                req.getEndTime().toLocalDate()
        ) + 1;

        int totalDaysInMonth = YearMonth.now().lengthOfMonth();
        double allowedDays = totalDaysInMonth * (ownership.getTotalSharePercentage() / 100.0);

        // ✅ Làm tròn xuống, không bao giờ vượt số ngày trong tháng
        allowedDays = Math.floor(allowedDays);

        double projectedUsageRatio = (usedDays + bookingDays) / allowedDays;

        if (projectedUsageRatio > 1.0) {
                throw new RuntimeException("Booking exceeds your monthly usage limit for this vehicle.");
        }

        // --- 5️⃣ Tính priority score (60% share – 40% usage) ---
        double shareScore = ownership.getTotalSharePercentage() / 100.0;
        double usageScore = 1 - (usedDays / allowedDays);
        double priorityScore = (0.6 * shareScore) + (0.4 * usageScore);

        // --- 6️⃣ Tạo đối tượng Booking mới ---
        Booking booking = Booking.builder()
                .user(user)
                .vehicle(vehicle)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .bookingStatus(BookingStatus.Pending)
                .priorityScore(priorityScore)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                // ✅ Thêm cờ tranh chấp
                .disputed(false)
                .disputeWinner(null)
                .cancelledAt(null)      // chưa hủy
                .blockSlot(false)       // chưa chiếm chỗ
                .build();

        // --- 7️⃣ Duyệt từng ngày trong chuỗi ---
        LocalDate startDate = req.getStartTime().toLocalDate();
        LocalDate endDate = req.getEndTime().toLocalDate();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

                Booking firstBooking = bookingRepository.findEarliestBookingForDate(vehicle.getVehicleId(), date);

                if (firstBooking == null) {
                // ✅ Chưa có ai đặt ngày này -> booking này là first
                continue;
                }

                long hoursSinceFirst = Duration.between(firstBooking.getCreatedAt(), booking.getCreatedAt()).toHours();
                long hoursUntilUse = Duration.between(LocalDateTime.now(), date.atTime(0, 0)).toHours();
                long windowHours = getWindowHoursForUse(hoursUntilUse);

                if (hoursSinceFirst > windowHours) {
                booking.setBookingStatus(BookingStatus.Cancelled);
                booking.setDisputed(true);
                booking.setDisputeWinner(false);
                continue;
                }

                // ✅ Lấy tất cả booking trong ngày (bao gồm cả booking mới)
                List<Booking> dayBookings = bookingRepository.findBookingsForDate(vehicle.getVehicleId(), date);
                dayBookings.add(booking); // 👈 Thêm booking mới vào danh sách để tranh chấp

                // ✅ Tìm booking có priority cao nhất
                Booking topBooking = dayBookings.stream()
                        .max(Comparator.comparing(Booking::getPriorityScore)
                                .thenComparing(Booking::getCreatedAt))
                        .orElse(booking);

                for (Booking b : dayBookings) {
                b.setDisputed(true); // có tranh chấp

                if (b.equals(topBooking)) {
                        b.setBookingStatus(BookingStatus.Pending); // staff confirm sau
                        b.setDisputeWinner(true);
                } else {
                        b.setBookingStatus(BookingStatus.Cancelled);
                        b.setDisputeWinner(false);
                }

                bookingRepository.save(b);
                }
        }

        // --- 8️⃣ Lưu booking mới ---
        bookingRepository.save(booking);
        return mapToResponse(booking);
        }



        private long getWindowHoursForUse(long hoursUntilUse) {
                if (hoursUntilUse <= 4) return 0;
                if (hoursUntilUse <= 24) return 4;
                if (hoursUntilUse <= 72) return 20;
                if (hoursUntilUse <= 168) return 48;
                return 72; // quá xa -> không cho tranh chấp
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

        public List<BookingResponse> getDisputedBookingsByVehicle(Long vehicleId) {
    List<Booking> disputedBookings = bookingRepository.findByVehicle_VehicleIdAndDisputedTrue(vehicleId);

    if (disputedBookings.isEmpty()) {
        throw new RuntimeException("No disputed bookings found for this vehicle.");
    }

    return disputedBookings.stream()
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
                booking.setCancelledAt(LocalDateTime.now());

                // ✅ Nếu booking từng là người thắng tranh chấp, vẫn giữ quyền "chiếm chỗ"
                if (booking.isDisputed() && Boolean.TRUE.equals(booking.getDisputeWinner())) {
                booking.setBlockSlot(true); // flag mới để ghi nhận ngày đó đã được chiếm
                }

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
        private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .vehicleId(booking.getVehicle().getVehicleId())
                .vehicleName(booking.getVehicle().getModel()+booking.getVehicle().getPlateNumber())
                .userName(booking.getUser().getFullName())
                .userEmail(booking.getUser().getEmail())
                .bookingStatus(booking.getBookingStatus().name())
                .priorityScore(booking.getPriorityScore())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .createdAt(booking.getCreatedAt())
                .disputed(booking.isDisputed())
                .disputeWinner(booking.getDisputeWinner())
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
