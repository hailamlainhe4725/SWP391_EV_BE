package com.example.demo.service;

import com.example.demo.dto.response.OwnershipResponse;
import com.example.demo.dto.response.OwnershipVehicleResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OwnershipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final UserRepository userRepository;
    private final VehicleService vehicleService;
    private final OwnershipRepository ownershipRepository;
    private final VehicleRepository vehicleRepository;

    // === Lấy tất cả Ownership ===
    public List<OwnershipResponse> getAll() {
        return ownershipRepository.findAll()
                .stream()
                .peek(this::resetIfNewMonth)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // === Lấy Ownership theo email ===
    public List<OwnershipResponse> getByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ownershipRepository.findByUser_Id(user.getId())
                .stream()
                .peek(this::resetIfNewMonth)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // === Lấy danh sách xe mà user đang sở hữu ===
    public List<VehicleResponse> getVehicleInMyOwnership(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ownershipRepository.findByUser_Id(user.getId()).stream()
                .peek(this::resetIfNewMonth)
                .map(Ownership::getVehicle)
                .map(vehicleService::mapToResponse)
                .collect(Collectors.toList());
    }

    // === Lấy danh sách thành viên cùng sở hữu một xe ===
    public List<OwnershipResponse> getGroupOwnership(Authentication auth, Long vehicleId) {
        userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

Vehicle vehicle = vehicleRepository.findByVehicleIdAndDeletedFalse(vehicleId)
    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found or deleted"));

        List<Ownership> ownershipList = ownershipRepository.findByVehicle_VehicleId(vehicle.getVehicleId());
        if (ownershipList.isEmpty()) {
            throw new RuntimeException("This vehicle is not in Ownership");
        }

        return ownershipList.stream()
                .peek(this::resetIfNewMonth)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // === Lấy danh sách xe + Ownership mà user đang sở hữu ===
    public List<OwnershipVehicleResponse> getMyOwnershipVehicles(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ownershipRepository.findByUser_IdAndDeletedFalse(user.getId())
                .stream()
                .peek(this::resetIfNewMonth)
                .map(this::mapToOwnershipVehicleResponse)
                .collect(Collectors.toList());
    }

    // === Map Entity → DTO ===
    private OwnershipResponse mapToResponse(Ownership o) {
        return OwnershipResponse.builder()
                .ownershipId(o.getOwnershipId())
                .userName(o.getUser().getEmail())
                .vehicleName(o.getVehicle().getBrand() + " " + o.getVehicle().getModel())
                .totalSharePercentage(o.getTotalSharePercentage())
                .status(o.getStatus().name())
                .createdAt(o.getCreatedAt())
                .allowedDaysThisMonth(o.getAllowedDaysThisMonth())
                .allowedKmThisMonth(o.getAllowedKmThisMonth())
                .usedDaysThisMonth(o.getUsedDaysThisMonth())
                .usedKmThisMonth(o.getUsedKmThisMonth())
                .build();
    }

    private OwnershipVehicleResponse mapToOwnershipVehicleResponse(Ownership ownership) {
        Vehicle v = ownership.getVehicle();

        return OwnershipVehicleResponse.builder()
                .ownershipId(ownership.getOwnershipId())
                .totalSharePercentage(ownership.getTotalSharePercentage())
                .status(ownership.getStatus())
                .createdAt(ownership.getCreatedAt())
                .allowedKmThisMonth(ownership.getAllowedKmThisMonth())
                .usedKmThisMonth(ownership.getUsedKmThisMonth())
                .allowedDaysThisMonth(ownership.getAllowedDaysThisMonth())
                .usedDaysThisMonth(ownership.getUsedDaysThisMonth())

                .vehicleId(v.getVehicleId())
                .brand(v.getBrand())
                .model(v.getModel())
                .plateNumber(v.getPlateNumber())
                .color(v.getColor())
                .year(v.getYear())
                .batteryCapacityKwh(v.getBatteryCapacityKwh())
                .description(v.getDescription())
                .imageUrl(v.getImageUrl())
                .vehicleStatus(v.getStatus())
                .feeChargingPerKwh(v.getFeeChargingPer1PercentUsed())
                .feeOverKm(v.getFeeOverKm())
                .operationPerM(v.getOperationPerMonthPerShare())
                .build();
    }

    // === Tự động reset usage khi sang tháng mới ===
    private void resetIfNewMonth(Ownership ownership) {
        int currentMonth = LocalDate.now().getMonthValue();

        if (ownership.getLastResetMonth() == null || !ownership.getLastResetMonth().equals(currentMonth)) {
            double baseKmLimit = 3000.0; // Giới hạn mặc định mỗi tháng cho 100% cổ phần
            ownership.resetMonthlyLimit(baseKmLimit);
            ownership.setLastResetMonth(currentMonth);
            ownershipRepository.save(ownership);
        }
    }
}
