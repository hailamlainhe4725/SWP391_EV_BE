package com.example.demo.service;

import com.example.demo.dto.request.CreateStaffCheckingRequest;
import com.example.demo.dto.request.StaffCheckingConfirmRequest;
import com.example.demo.dto.response.StaffCheckingResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    // === Lấy lịch sử CheckIn/CheckOut của user đang đăng nhập
    public List<StaffCheckingResponse> getByBookingAuthentication(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return staffCheckingRepository.findByUser_IdAndDeletedFalse(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // === Tạo Staff Checking mới (staff thực hiện)
    public StaffCheckingResponse create(Authentication authentication, CreateStaffCheckingRequest req) {
        // 1. Lấy staff từ token
        String staffEmail = authentication.getName();
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new RuntimeException("Staff not found with email: " + staffEmail));

        // 2. Lấy vehicle và booking
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 3. Lấy user (người thuê hoặc sở hữu)
        User user = userRepository.findByEmail(req.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + req.getUserEmail()));

        // 4. Kiểm tra trạng thái booking hợp lệ
        if (booking.getBookingStatus() != BookingStatus.Confirmed
                && booking.getBookingStatus() != BookingStatus.InProgress) {
            throw new RuntimeException("Booking must be confirmed before staff checking.");
        }

        // 5. Kiểm tra xem đã tồn tại checking loại này chưa
        Optional<StaffChecking> existing = staffCheckingRepository
                .findByBooking_BookingIdAndTypeAndDeletedFalse(booking.getBookingId(), req.getStaffCheckingType());
        if (existing.isPresent()) {
            throw new RuntimeException("A checking of this type already exists for this booking.");
        }

        // 6. Nếu là CheckIn → cần CheckOut được CONFIRMED trước đó
        if (req.getStaffCheckingType() == StaffCheckingType.CheckIn) {
            Optional<StaffChecking> confirmedCheckout = staffCheckingRepository
                    .findByBooking_BookingIdAndTypeAndStatusAndDeletedFalse(
                            booking.getBookingId(),
                            StaffCheckingType.CheckOut,
                            CheckingStatus.CONFIRMED
                    );
            if (confirmedCheckout.isEmpty()) {
                throw new RuntimeException("Cannot perform Check-In before Check-Out has been confirmed.");
            }
        }

        // 7. Upload chữ ký staff (nếu có)
        String staffSignatureUrl = uploadSignatureFile(req.getStaffSignature(), "staff");

        // 8. Tạo mới StaffChecking
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
        sc.setStaffSignatureUrl(staffSignatureUrl);

        staffCheckingRepository.save(sc);
        return mapToResponse(sc);
    }

    // === User xác nhận hoặc từ chối StaffChecking
    public StaffCheckingResponse confirm(Authentication authentication, Long id, StaffCheckingConfirmRequest req) {
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

        Booking booking = sc.getBooking();

        // Upload chữ ký user (nếu có)
        String userSignatureUrl = uploadSignatureFile(req.getStaffSignature(), "user");

        if (req.isApproved()) {
            sc.setStatus(CheckingStatus.CONFIRMED);
            sc.setUserSignatureUrl(userSignatureUrl);

            // Cập nhật trạng thái booking
            if (sc.getType() == StaffCheckingType.CheckOut) {
                booking.setBookingStatus(BookingStatus.InProgress);
            } else if (sc.getType() == StaffCheckingType.CheckIn) {
                booking.setBookingStatus(BookingStatus.Completed);
            }
            bookingRepository.save(booking);

            // === Logic tính toán phí biến động (VariableFee)
            if (sc.getType() == StaffCheckingType.CheckIn) {
                StaffChecking checkout = staffCheckingRepository
                        .findByBooking_BookingIdAndTypeAndDeletedFalse(
                                sc.getBooking().getBookingId(), StaffCheckingType.CheckOut)
                        .orElseThrow(() -> new RuntimeException("Missing CheckOut record"));

                Double distanceTraveled = sc.getOdometer() - checkout.getOdometer();
                Double batteryUsed = checkout.getBatteryPercent() - sc.getBatteryPercent();

                Ownership ownership = ownershipRepository
                        .findByUser_IdAndVehicle_VehicleId(sc.getUser().getId(), sc.getVehicle().getVehicleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ownership not found"));

                ownership.setUsedKmThisMonth(ownership.getUsedKmThisMonth() + distanceTraveled);

                long daysUsed = ChronoUnit.DAYS.between(
                        booking.getStartTime().toLocalDate(),
                        booking.getEndTime().toLocalDate()) + 1;
                ownership.setUsedDaysThisMonth(ownership.getUsedDaysThisMonth() + daysUsed);
                ownershipRepository.save(ownership);

                double allowedKm = ownership.getAllowedKmThisMonth();
                if (ownership.getUsedKmThisMonth() > allowedKm) {
                    double exceededKm = ownership.getUsedKmThisMonth() - allowedKm;
                    variableFeeRepository.save(VariableFee.builder()
                            .vehicle(sc.getVehicle())
                            .user(sc.getUser())
                            .booking(sc.getBooking())
                            .staffChecking(sc)
                            .type(VariableFeeType.OverOdometer)
                            .amount(exceededKm * sc.getVehicle().getFeeOverKm())
                            .description("Exceeded allowed kilometers")
                            .createdAt(LocalDateTime.now())
                            .recordedBy(sc.getStaff())
                            .deleted(false)
                            .build());
                }

                if (Boolean.TRUE.equals(sc.getDamageReported())) {
                    variableFeeRepository.save(VariableFee.builder()
                            .vehicle(sc.getVehicle())
                            .user(sc.getUser())
                            .booking(sc.getBooking())
                            .staffChecking(sc)
                            .type(VariableFeeType.Damage)
                            .amount(10000.0)
                            .description("Damage reported")
                            .createdAt(LocalDateTime.now())
                            .recordedBy(sc.getStaff())
                            .deleted(false)
                            .build());
                }

                variableFeeRepository.save(VariableFee.builder()
                        .vehicle(sc.getVehicle())
                        .user(sc.getUser())
                        .booking(sc.getBooking())
                        .staffChecking(sc)
                        .type(VariableFeeType.Charging)
                        .amount((batteryUsed / 100) * sc.getVehicle().getBatteryCapacityKwh() * 2500)
                        .description("Battery recharge after trip")
                        .createdAt(LocalDateTime.now())
                        .recordedBy(sc.getStaff())
                        .deleted(false)
                        .build());
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

    // === Upload chữ ký (dùng chung)
private String uploadSignatureFile(MultipartFile file, String prefix) {
    if (file == null || file.isEmpty()) {
        System.out.println("⚠️ No file uploaded for " + prefix);
        return null;
    }

    try {
        String uploadDir = "uploads/signatures/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = prefix + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        file.transferTo(filePath);

        System.out.println("✅ Saved signature file: " + fileName);
        return "/uploads/signatures/" + fileName;
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to save signature file", e);
    }
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
                .userSignature(sc.getUserSignatureUrl())
                .staffSignature(sc.getStaffSignatureUrl())
                .build();
    }
}
