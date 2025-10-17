package com.example.demo.service;

import com.example.demo.dto.request.CreateStaffCheckingRequest;
import com.example.demo.dto.response.StaffCheckingResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.StaffCheckingType;
import com.example.demo.enums.VariableFeeType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;

import com.example.demo.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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

        // === Staff tạo CheckIn hoặc CheckOut ===
        public StaffCheckingResponse create(Authentication authentication,CreateStaffCheckingRequest req) {
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
                User staff = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new ResourceNotFoundException("staff not found"));
                User user = userRepository.findByEmail(req.getUserEmail())
                                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
                Booking booking = bookingRepository.findById(req.getBookingId())
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                Ownership ownership = ownershipRepository
                                .findByUser_IdAndVehicle_VehicleId(user.getId(), vehicle.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Ownership not found"));

                StaffCheckingType type = req.getStaffCheckingType();

                // Lấy các lần check trước đó
                List<StaffChecking> existing = staffCheckingRepository.findByBookingAndDeletedFalse(booking);

                // === CASE 1: CheckOut (staff giao xe) ===
                if (type == StaffCheckingType.CheckOut) {
                        boolean hasCheckOut = existing.stream()
                                        .anyMatch(c -> c.getType() == StaffCheckingType.CheckOut);
                        if (hasCheckOut)
                                throw new RuntimeException("This booking has already been checked out.");

                        StaffChecking checkOut = StaffChecking.builder()
                                        .vehicle(vehicle)
                                        .user(user)
                                        .staff(staff)
                                        .booking(booking)
                                        .type(StaffCheckingType.CheckOut)
                                        .odometer(req.getOdometer())
                                        .batteryPercent(req.getBatteryPercent())
                                        .damageReported(req.getDamageReported())
                                        .notes(req.getNotes())
                                        .checkTime(LocalDateTime.now())
                                        .deleted(false)
                                        .build();


                        staffCheckingRepository.save(checkOut);

                            booking.setBookingStatus(BookingStatus.Completed);
                                bookingRepository.save(booking);
                        return mapToResponse(checkOut);
                }

                // === CASE 2: CheckIn ===
                if (type == StaffCheckingType.CheckIn) {
                        StaffChecking checkout = existing.stream()
                                        .filter(c -> c.getType() == StaffCheckingType.CheckOut)
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException(
                                                        "You must perform CheckOut before CheckIn."));

                        Double distanceTraveled = (req.getOdometer() != null && checkout.getOdometer() != null)
                                        ? req.getOdometer() - checkout.getOdometer()
                                        : null;

                        Double batteryUsed = (req.getBatteryPercent() != null && checkout.getBatteryPercent() != null)
                                        ? checkout.getBatteryPercent() - req.getBatteryPercent()
                                        : null;

                        // --- Cập nhật ownership ---
                        ownership = ownershipRepository
                                        .findByUser_IdAndVehicle_VehicleId(user.getId(), vehicle.getVehicleId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Ownership not found"));

                        if (distanceTraveled != null) {
                                ownership.setUsedKmThisMonth(ownership.getUsedKmThisMonth() + distanceTraveled);
                                ownershipRepository.save(ownership);
                        }

                        double allowedKm = ownership.getAllowedKmThisMonth();
                        if (ownership.getUsedKmThisMonth() > allowedKm) {
                                double exceededKm = ownership.getUsedKmThisMonth() - allowedKm;

                                // === Tạo phí vượt km ===
                                VariableFee overKmFee = VariableFee.builder()
                                                .vehicle(vehicle)
                                                .user(user)
                                                .booking(booking)
                                                .type(VariableFeeType.OverOdometer)
                                                .amount(exceededKm * vehicle.getOperatingCostPerKm())
                                                .description("Exceeded allowed kilometers for this month")
                                                .createdAt(LocalDateTime.now())
                                                .recordedBy(staff)
                                                .deleted(false)
                                                .build();
                                variableFeeRepository.save(overKmFee);

                                // Hủy booking tương lai
                                List<Booking> future = bookingRepository
                                                .findByUserAndVehicleAndStartTimeAfter(user, vehicle,
                                                                LocalDateTime.now());
                                for (Booking b : future) {
                                        b.setBookingStatus(com.example.demo.enums.BookingStatus.Cancelled);
                                }
                                bookingRepository.saveAll(future);
                        }

                        // === Tạo phí hư hại nếu có ===
                        if (req.getDamageReported() != null && !req.getDamageReported().FALSE) {
                                VariableFee damageFee = VariableFee.builder()
                                                .vehicle(vehicle)
                                                .user(user)
                                                .booking(booking)
                                                .type(VariableFeeType.Damage)
                                                .amount(10000.000) // tạm ước lượng
                                                .description("Damage reported: " + req.getDamageReported())
                                                .createdAt(LocalDateTime.now())
                                                .recordedBy(staff)
                                                .deleted(false)
                                                .build();
                                variableFeeRepository.save(damageFee);
                        }

                        // === Tạo phí sạc pin cố định (sau mỗi chuyến) ===
                        VariableFee chargingFee = VariableFee.builder()
                                        .vehicle(vehicle)
                                        .user(user)
                                        .booking(booking)
                                        .type(VariableFeeType.Charging)
                                        .amount((batteryUsed / 100) * vehicle.getBatteryCapacityKwh() * 2500) // ví dụ:
                                                                                                              // VND
                                        .description("Vehicle battery recharge after trip (100%)")
                                        .createdAt(LocalDateTime.now())
                                        .recordedBy(staff)
                                        .deleted(false)
                                        .build();
                        variableFeeRepository.save(chargingFee);

                        // === Tạo CheckIn ===
                        StaffChecking checkIn = StaffChecking.builder()
                                        .vehicle(vehicle)
                                        .user(user)
                                        .staff(staff)
                                        .booking(booking)
                                        .type(StaffCheckingType.CheckIn)
                                        .odometer(req.getOdometer())
                                        .batteryPercent(req.getBatteryPercent())
                                        .damageReported(req.getDamageReported())
                                        .notes(req.getNotes())
                                        .distanceTraveled(distanceTraveled)
                                        .batteryUsedPercent(batteryUsed)
                                        .deleted(false)
                                        .build();

                        staffCheckingRepository.save(checkIn);
                        return mapToResponse(checkIn);
                }

                throw new RuntimeException("Invalid check type");
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
                                .build();
        }
}
