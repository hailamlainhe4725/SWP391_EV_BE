package com.example.demo.service;

import com.example.demo.dto.request.CreateStaffCheckingRequest;
import com.example.demo.dto.response.StaffCheckingResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffCheckingService {

        private final StaffCheckingRepository staffCheckingRepository;
        private final UserRepository userRepository;
        private final VehicleRepository vehicleRepository;
        private final BookingRepository bookingRepository;

        // === Staff: lấy toàn bộ checking
        public List<StaffCheckingResponse> getAll() {
                return staffCheckingRepository.findByDeletedFalse().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // === User: xem lịch sử checkin/checkout theo booking
        public List<StaffCheckingResponse> getByBooking(Long bookingId) {
                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
                return staffCheckingRepository.findByBookingAndDeletedFalse(booking)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // === Staff: tạo mới checking (CheckIn hoặc CheckOut)
        public StaffCheckingResponse create(CreateStaffCheckingRequest req) {
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
                User user = userRepository.findById(req.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                User staff = userRepository.findById(req.getStaffId())
                                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
                Booking booking = bookingRepository.findById(req.getBookingId())
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                // Nếu là CheckIn thì tự động tính toán thông tin
                Double distanceTraveled = null;
                Double batteryUsedPercent = null;

                if (req.getCheckType().equalsIgnoreCase("CheckIn")) {
                        // Lấy CheckOut đầu tiên của cùng booking (nếu có)
                        StaffChecking checkout = staffCheckingRepository.findByBookingAndDeletedFalse(booking).stream()
                                        .filter(sc -> "CheckOut".equalsIgnoreCase(sc.getCheckType()))
                                        .findFirst().orElse(null);

                        if (checkout != null && req.getOdometer() != null && checkout.getOdometer() != null) {
                                distanceTraveled = (double) req.getOdometer() - checkout.getOdometer();
                        }

                        if (checkout != null && req.getBatteryPercent() != null
                                        && checkout.getBatteryPercent() != null) {
                                batteryUsedPercent = (double) checkout.getBatteryPercent() - req.getBatteryPercent();
                        }
                }

                StaffChecking checking = StaffChecking.builder()
                                .vehicle(vehicle)
                                .user(user)
                                .staff(staff)
                                .booking(booking)
                                .checkType(req.getCheckType())
                                .odometer(req.getOdometer())
                                .batteryPercent(req.getBatteryPercent())
                                .damageReported(req.getDamageReported())
                                .notes(req.getNotes())
                                .distanceTraveled(distanceTraveled)
                                .batteryUsedPercent(batteryUsedPercent)
                                .deleted(false)
                                .build();

                staffCheckingRepository.save(checking);
                return mapToResponse(checking);
        }

        // === Staff: soft delete
        public void softDelete(Long id) {
                StaffChecking sc = staffCheckingRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Staff checking not found"));
                sc.setDeleted(true);
                staffCheckingRepository.save(sc);
        }

        // === Mapper
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
                                .checkType(sc.getCheckType())
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
