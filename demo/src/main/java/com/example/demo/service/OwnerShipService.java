package com.example.demo.service;

import com.example.demo.dto.request.CreateOwnershipRequest;
import com.example.demo.dto.response.OwnershipResponse;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OwnershipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnershipService {

    private final OwnershipRepository ownershipRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * For staff: get all ownership records (not deleted)
     */
    public List<OwnershipResponse> getAll() {
        return ownershipRepository.findByDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * For user: get ownerships by user email
     */
    public List<OwnershipResponse> getByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return ownershipRepository.findByUserAndDeletedFalse(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create new ownership (staff)
     */
    public OwnershipResponse create(CreateOwnershipRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + req.getUserId()));
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + req.getVehicleId()));

        Ownership o = Ownership.builder()
                .user(user)
                .vehicle(vehicle)
                .totalSharePercentage(req.getTotalSharePercentage() != null ? req.getTotalSharePercentage() : 0.0)
                .status(req.getStatus() != null ? req.getStatus() : "Active")
                .deleted(false)
                .build();

        Ownership saved = ownershipRepository.save(o);
        return mapToResponse(saved);
    }

    /**
     * Update ownership (staff)
     * Controller currently passes CreateOwnershipRequest for update — kept for
     * compatibility.
     */
    public OwnershipResponse update(Long id, CreateOwnershipRequest req) {
        Ownership o = ownershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership not found with id: " + id));

        // Only update fields provided in request
        if (req.getUserId() != null && !req.getUserId().equals(o.getUser().getId())) {
            User u = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + req.getUserId()));
            o.setUser(u);
        }

        if (req.getVehicleId() != null && !req.getVehicleId().equals(o.getVehicle().getVehicleId())) {
            Vehicle v = vehicleRepository.findById(req.getVehicleId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Vehicle not found with id: " + req.getVehicleId()));
            o.setVehicle(v);
        }

        if (req.getTotalSharePercentage() != null) {
            o.setTotalSharePercentage(req.getTotalSharePercentage());
        }

        if (req.getStatus() != null) {
            o.setStatus(req.getStatus());
        }

        Ownership saved = ownershipRepository.save(o);
        return mapToResponse(saved);
    }

    /**
     * Soft delete
     */
    public void softDelete(Long id) {
        Ownership o = ownershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership not found with id: " + id));
        o.setDeleted(true);
        ownershipRepository.save(o);
    }

    /**
     * Mapper to DTO
     * NOTE: uses user.getId() and vehicle.getVehicleId() — adapt if your entity
     * getter names differ.
     */
    private OwnershipResponse mapToResponse(Ownership o) {
        Long userId = o.getUser() != null ? o.getUser().getId() : null;
        String userFullName = o.getUser() != null ? o.getUser().getFullName() : null;
        Long vehicleId = o.getVehicle() != null ? o.getVehicle().getVehicleId() : null;
        String vehicleModel = o.getVehicle() != null ? o.getVehicle().getModel() : null;

        return OwnershipResponse.builder()
                .ownershipId(o.getOwnershipId())
                .userId(userId)
                .userName(userFullName)
                .vehicleId(vehicleId)
                .vehicleModel(vehicleModel)
                .totalSharePercentage(o.getTotalSharePercentage())
                .status(o.getStatus())
                .build();
    }
}
