package com.example.demo.service;

import com.example.demo.dto.response.OwnershipResponse;
import com.example.demo.dto.response.OwnershipVehicleResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OwnershipRepository;
import lombok.RequiredArgsConstructor;

import org.hibernate.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;


import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final UserRepository userRepository;
    private final VehicleService vehicleService;
    private final OwnershipRepository ownershipRepository;
    private final VehicleRepository vehicleRepository;
   


    public List<OwnershipResponse> getAll() {
        return ownershipRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OwnershipResponse> getByUserEmail(String email) {
                    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ownershipRepository.findByUser_Id(user.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

public List<VehicleResponse>getVehicleInMyOwnership(Authentication auth){
    String email = auth.getName();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<VehicleResponse> vehicles = ownershipRepository.findByUser_Id(user.getId()).stream()
        .map(Ownership::getVehicle)
        .map(vehicle -> vehicleService.mapToResponse(vehicle))
        .collect(Collectors.toList());

    return vehicles;
}

    public List<OwnershipResponse> getGroupOwnership(Authentication auth,@PathVariable Long id) {
            User user = userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        List<Ownership> ownershipList = ownershipRepository.findByVehicle_VehicleId(vehicle.getVehicleId());
        if(ownershipList.isEmpty()){
            throw new RuntimeException("this vehicle not in Ownership");
        }


        return ownershipList
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private OwnershipResponse mapToResponse(Ownership o) {
        return OwnershipResponse.builder()
                .ownershipId(o.getOwnershipId())
                .userName(o.getUser().getFullName())
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


     // === Lấy danh sách xe mà user đang sở hữu ===
    public List<OwnershipVehicleResponse> getMyOwnershipVehicles(Authentication authentication) {
       User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ownershipRepository.findByUser_IdAndDeletedFalse(user.getId())
                .stream()
                .map(this::mapToOwnershipVehicleResponse)
                .collect(Collectors.toList());
    }

    // === Map Entity → DTO ===
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
                .operatingCostPerDay(v.getOperatingCostPerDay())
                .operatingCostPerKm(v.getOperatingCostPerKm())
                .description(v.getDescription())
                .imageUrl(v.getImageUrl()) // nếu có field này
                .vehicleStatus(v.getStatus())
                .build();
    }
    
}
