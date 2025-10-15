package com.example.demo.service;

import com.example.demo.dto.response.OwnershipResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OwnershipRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


import com.example.demo.repository.UserRepository;
@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final UserRepository userRepository;
    private final VehicleService vehicleService;
    private final OwnershipRepository ownershipRepository;


    public List<OwnershipResponse> getAll() {
        return ownershipRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OwnershipResponse> getByUserEmail(String email) {
        return ownershipRepository.findByUser_Email(email)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

public List<VehicleResponse>getVehicleInMyOwnership(Authentication auth){
    String email = auth.getName();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<VehicleResponse> vehicles = ownershipRepository.findByUser_Email(user.getEmail()).stream()
        .map(Ownership::getVehicle)
        .map(vehicle -> vehicleService.mapToResponse(vehicle))
        .collect(Collectors.toList());

    return vehicles;
}


    private OwnershipResponse mapToResponse(Ownership o) {
        return OwnershipResponse.builder()
                .ownershipId(o.getOwnershipId())
                .userName(o.getUser().getFullName())
                .vehicleName(o.getVehicle().getBrand() + " " + o.getVehicle().getModel())
                .totalSharePercentage(o.getTotalSharePercentage())
                .status(o.getStatus().name())
                .createdAt(o.getCreatedAt())
                .build();
    }
}
