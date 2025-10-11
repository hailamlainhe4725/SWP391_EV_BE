package com.example.demo.service;

import com.example.demo.dto.request.CreateContractRequest;
import com.example.demo.dto.response.ContractResponse;
import com.example.demo.entity.Contract;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.enums.ContractStatus;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public List<ContractResponse> getAll() {
        return contractRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ContractResponse getById(Long id) {
        return contractRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    public ContractResponse create(CreateContractRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        Contract contract = Contract.builder()
                .user(user)
                .vehicle(vehicle)
                .salePercentage(req.getSalePercentage())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(ContractStatus.PENDING)
                .build();

        contractRepository.save(contract);
        return mapToResponse(contract);
    }

    public ContractResponse updateStatus(Long id, ContractStatus status) {
        Contract c = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        c.setStatus(status);
        contractRepository.save(c);
        return mapToResponse(c);
    }

    public void softDelete(Long id) {
        Contract c = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        c.setStatus(ContractStatus.EXPIRED);
        contractRepository.save(c);
    }

    private ContractResponse mapToResponse(Contract c) {
        return ContractResponse.builder()
                .contractId(c.getContractId())
                .ownerName(c.getUser().getFullName())
                .vehicleName(c.getVehicle().getBrand() + " " + c.getVehicle().getModel())
                .salePercentage(c.getSalePercentage())
                .status(c.getStatus().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
