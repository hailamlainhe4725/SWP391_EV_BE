package com.example.demo.service;

import com.example.demo.entity.OwnerShip;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OwnershipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OwnerShipService {
    private final OwnershipRepository ownershipRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public OwnerShip create(Long userId, Long vehicleId, OwnerShip ownership) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        ownership.setUser(user);
        ownership.setVehicle(vehicle);
        return ownershipRepository.save(ownership);
    }

    public List<OwnerShip> getAll() {
        return ownershipRepository.findAll();
    }

    public OwnerShip getById(Long id) {
        return ownershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership not found"));
    }
}
