package com.example.demo.service;

import com.example.demo.dto.request.CreateContractRequest;
import com.example.demo.dto.response.ContractResponse;
import com.example.demo.entity.Contract;
import com.example.demo.entity.OwnerContract;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.enums.ContractStatus;
import com.example.demo.enums.OwnerContractStatus;
import com.example.demo.enums.OwnershipStatus;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final OwnershipRepository ownershipRepository;

    private final OwnerContractRepository ownerContractRepository;

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

        // ===== 1. Tạo Contract =====
        Contract contract = Contract.builder()
                .user(user)
                .vehicle(vehicle)
                .salePercentage(req.getSalePercentage())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(req.getStatus() != null ? req.getStatus() : ContractStatus.PENDING)
                .build();

        contractRepository.save(contract);

        // ===== 2. Nếu Contract được APPROVED, tạo OwnerContract luôn =====
        if (contract.getStatus() == ContractStatus.APPROVED) {
                OwnerContract ownerContract = OwnerContract.builder()
                        .contract(contract)
                        .user(user)
                        .sharePercentage(req.getSalePercentage())
                        .status(OwnerContractStatus.ACTIVE)
                        .build();
                ownerContractRepository.save(ownerContract);

                // ===== 3. Tạo hoặc cập nhật Ownership =====
                Ownership ownership = ownershipRepository
                        .findByUser_IdAndVehicle_VehicleId(user.getId(), vehicle.getVehicleId())
                        .orElse(Ownership.builder()
                                .user(user)
                                .vehicle(vehicle)
                                .totalSharePercentage(0.0)
                                .status(OwnershipStatus.ACTIVE)
                                .build());

                ownership.setTotalSharePercentage(
                        ownership.getTotalSharePercentage() + req.getSalePercentage()
                );

                // Tính quyền sử dụng theo tỷ lệ cổ phần
                double allowedDaysPerMonth = (30.0 / 12.0) * (ownership.getTotalSharePercentage() / 10.0);
                double allowedKmPerMonth = (3000.0 / 12.0) * (ownership.getTotalSharePercentage() / 10.0);
                ownership.setAllowedDaysThisMonth(allowedDaysPerMonth);
                ownership.setAllowedKmThisMonth(allowedKmPerMonth);
                ownership.setUsedDaysThisMonth(0.0);
                ownership.setUsedKmThisMonth(0.0);

                ownershipRepository.save(ownership);
        }

        // ===== 4. Trả về response =====
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
