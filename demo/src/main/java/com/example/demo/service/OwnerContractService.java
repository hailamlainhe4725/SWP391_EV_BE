package com.example.demo.service;

import com.example.demo.dto.request.CreateOwnerContractRequest;
import com.example.demo.dto.response.OwnerContractResponse;
import com.example.demo.entity.OwnerContract;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.repository.OwnerContractRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerContractService {

    private final OwnerContractRepository ownerContractRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public List<OwnerContractResponse> getAll() {
        return ownerContractRepository.findByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OwnerContractResponse create(CreateOwnerContractRequest req) {
        OwnerContract contract = new OwnerContract();
        contract.setOwnerName(req.getOwnerName());
        contract.setStartDate(req.getStartDate());
        contract.setEndDate(req.getEndDate());
        contract.setDeleted(false);

        if (req.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            contract.setVehicle(vehicle);
        }

        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            contract.setUser(user);
        }

        ownerContractRepository.save(contract);
        return toResponse(contract);
    }

    public OwnerContractResponse update(Long id, CreateOwnerContractRequest req) {
        OwnerContract contract = ownerContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OwnerContract not found"));

        contract.setOwnerName(req.getOwnerName());
        contract.setStartDate(req.getStartDate());
        contract.setEndDate(req.getEndDate());

        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            contract.setUser(user);
        }

        if (req.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            contract.setVehicle(vehicle);
        }

        ownerContractRepository.save(contract);
        return toResponse(contract);
    }

    public void softDelete(Long id) {
        OwnerContract contract = ownerContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OwnerContract not found"));
        contract.setDeleted(true);
        ownerContractRepository.save(contract);
    }

    private OwnerContractResponse toResponse(OwnerContract c) {
        OwnerContractResponse res = new OwnerContractResponse();
        res.setId(c.getId());
        res.setOwnerName(c.getOwnerName());
        res.setStartDate(c.getStartDate());
        res.setEndDate(c.getEndDate());
        res.setVehicleId(c.getVehicle() != null ? c.getVehicle().getVehicleId() : null);
        res.setUserEmail(c.getUser() != null ? c.getUser().getEmail() : null);
        return res;
    }
}
