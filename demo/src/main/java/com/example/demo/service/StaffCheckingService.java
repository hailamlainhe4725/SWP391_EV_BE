package com.example.demo.service;

import com.example.demo.dto.request.CreateStaffCheckingRequest;
import com.example.demo.dto.request.StaffCheckingConfirmRequest;
import com.example.demo.dto.response.StaffCheckingResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.CheckingStatus;
import com.example.demo.enums.StaffCheckingType;
import com.example.demo.enums.VariableFeeType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StaffCheckingService {

        private final StaffCheckingRepository staffCheckingRepository;
        private final UserRepository userRepository;
        private final VehicleRepository vehicleRepository;
        private final BookingRepository bookingRepository;
        private final OwnershipRepository ownershipRepository;
        private final VariableFeeRepository variableFeeRepository;
        // === Lấy toàn bộ Staff Checking (staff dùng)
        public List<StaffCheckingResponse> getAll() {
                return staffCheckingRepository.findByDeletedFalse().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // === Xem lịch sử CheckIn/CheckOut theo Booking
        public List<StaffCheckingResponse> getByBooking(Long bookingId) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                return staffCheckingRepository.findByBookingAndDeletedFalse(booking)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<StaffCheckingResponse> getByBookingAuthentication(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return staffCheckingRepository.findByUser_IdAndDeletedFalse(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        }

       public StaffCheckingResponse create(Authentication authentication, CreateStaffCheckingRequest req) {
    // 🔹 Lấy thông tin staff từ token đăng nhập (email)
    String staffEmail = authentication.getName();
    User staff = userRepository.findByEmail(staffEmail)
            .orElseThrow(() -> new RuntimeException("Staff not found with email: " + staffEmail));

    // 🔹 Lấy vehicle
    Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Vehicle not found"));

    // 🔹 Lấy booking
    Booking booking = bookingRepository.findById(req.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found"));

    // 🔹 Lấy user (người thuê xe hoặc sở hữu)
    User user = userRepository.findByEmail(req.getUserEmail())
            .orElseThrow(() -> new RuntimeException("User not found with email: " + req.getUserEmail()));

    // 🔹 Tạo mới StaffChecking
    StaffChecking sc = new StaffChecking();
    sc.setVehicle(vehicle);
    sc.setBooking(booking);
    sc.setUser(user);
    sc.setStaff(staff);
    sc.setType(req.getStaffCheckingType());
    sc.setCheckTime(LocalDateTime.now());
    sc.setOdometer(req.getOdometer());
    sc.setBatteryPercent(req.getBatteryPercent());
    sc.setDamageReported(req.getDamageReported());
    sc.setNotes(req.getNotes());
    sc.setStatus(CheckingStatus.PENDING);
    sc.setUserComment(null);

    // 🔹 (Tùy chọn) Nếu muốn lưu ảnh signature sau này thì thêm xử lý upload
    // if (req.getStaffSignature() != null) { ... }
        booking.setBookingStatus(BookingStatus.Completed);
    staffCheckingRepository.save(sc);

    // 🔹 Trả về response
    return mapToResponse(sc);
}

        public StaffCheckingResponse confirm(Authentication authentication,Long id,StaffCheckingConfirmRequest req) {
    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    StaffChecking sc = staffCheckingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Checking not found"));

    if (!sc.getUser().getId().equals(user.getId())) {
        throw new RuntimeException("You cannot confirm this record");
    }

    if (sc.getStatus() != CheckingStatus.PENDING) {
        throw new RuntimeException("Already confirmed");
    }

    if (req.isApproved()) {
    sc.setStatus(CheckingStatus.CONFIRMED);

    // === Chỉ xử lý khi user đồng ý ===
    if (sc.getType() == StaffCheckingType.CheckIn) {
        StaffChecking checkout = staffCheckingRepository
            .findByBooking_BookingIdAndTypeAndDeletedFalse(sc.getBooking().getBookingId(), StaffCheckingType.CheckOut)
            .orElseThrow(() -> new RuntimeException("Missing CheckOut record"));

        Double distanceTraveled = sc.getOdometer() - checkout.getOdometer();
        Double batteryUsed = checkout.getBatteryPercent() - sc.getBatteryPercent();

        Ownership ownership = ownershipRepository
            .findByUser_IdAndVehicle_VehicleId(sc.getUser().getId(), sc.getVehicle().getVehicleId())
            .orElseThrow(() -> new ResourceNotFoundException("Ownership not found"));

        ownership.setUsedKmThisMonth(ownership.getUsedKmThisMonth() + distanceTraveled);
        ownership.setUsedDaysThisMonth(ownership.getUsedDaysThisMonth() + 1);
        ownershipRepository.save(ownership);

        double allowedKm = ownership.getAllowedKmThisMonth();
        if (ownership.getUsedKmThisMonth() > allowedKm) {
            double exceededKm = ownership.getUsedKmThisMonth() - allowedKm;
            variableFeeRepository.save(VariableFee.builder()
                .vehicle(sc.getVehicle())
                .user(sc.getUser())
                .booking(sc.getBooking())
                .type(VariableFeeType.OverOdometer)
                .amount(exceededKm * sc.getVehicle().getOperatingCostPerKm())
                .description("Exceeded allowed kilometers")
                .createdAt(LocalDateTime.now())
                .recordedBy(sc.getStaff())
                .deleted(false)
                .build()
            );
        }

        if (Boolean.TRUE.equals(sc.getDamageReported())) {
            variableFeeRepository.save(VariableFee.builder()
                .vehicle(sc.getVehicle())
                .user(sc.getUser())
                .booking(sc.getBooking())
                .type(VariableFeeType.Damage)
                .amount(10000.0)
                .description("Damage reported")
                .createdAt(LocalDateTime.now())
                .recordedBy(sc.getStaff())
                .deleted(false)
                .build()
            );
        }

        variableFeeRepository.save(VariableFee.builder()
            .vehicle(sc.getVehicle())
            .user(sc.getUser())
            .booking(sc.getBooking())
            .type(VariableFeeType.Charging)
            .amount((batteryUsed / 100) * sc.getVehicle().getBatteryCapacityKwh() * 2500)
            .description("Battery recharge after trip")
            .createdAt(LocalDateTime.now())
            .recordedBy(sc.getStaff())
            .deleted(false)
            .build()
        );
    }
} else {
        sc.setStatus(CheckingStatus.REJECTED);
        sc.setUserComment(req.getUserComment());
    }

    staffCheckingRepository.save(sc);
    return mapToResponse(sc);
}


        // === Soft delete
        public void softDelete(Long id) {
                StaffChecking sc = staffCheckingRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Staff checking not found"));
                sc.setDeleted(true);
                staffCheckingRepository.save(sc);
        }

        // === Mapper ===
        private StaffCheckingResponse mapToResponse(StaffChecking sc) {
                return StaffCheckingResponse.builder()
                                .checkingId(sc.getCheckingId())
                                .vehicleId(sc.getVehicle().getVehicleId())
                                .vehicleModel(sc.getVehicle().getModel())
                                .userId(sc.getUser().getId())
                                .userName(sc.getUser().getFullName())
                                .staffId(sc.getStaff().getId())
                                .staffName(sc.getStaff().getFullName())
                                .bookingId(sc.getBooking().getBookingId())
                                .checkingType(sc.getType())
                                .checkTime(sc.getCheckTime())
                                .odometer(sc.getOdometer())
                                .batteryPercent(sc.getBatteryPercent())
                                .damageReported(sc.getDamageReported())
                                .notes(sc.getNotes())
                                .distanceTraveled(sc.getDistanceTraveled())
                                .batteryUsedPercent(sc.getBatteryUsedPercent())
                                
                                .userSignature(sc.getUser().getSignatureImageUrl())
                                .staffSignature(sc.getStaff().getSignatureImageUrl())

                                .build();
        }
}
