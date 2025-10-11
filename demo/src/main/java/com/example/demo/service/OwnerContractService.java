package com.example.demo.service;

import com.example.demo.dto.request.CreateOwnerContractRequest;
import com.example.demo.dto.response.OwnerContractResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerContractService {

    private final OwnerContractRepository ownerContractRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final OwnershipRepository ownershipRepository;

    public List<OwnerContractResponse> getAll() {
        return ownerContractRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OwnerContractResponse create(CreateOwnerContractRequest req) {
        Contract contract = contractRepository.findById(req.getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (contract.getStatus() != ContractStatus.APPROVED) {
            throw new IllegalStateException("Contract must be approved before adding owner contracts");
        }

        double totalShare = ownerContractRepository.findByContract_ContractId(contract.getContractId())
                .stream().mapToDouble(OwnerContract::getSharePercentage).sum();

        if (totalShare + req.getSharePercentage() > contract.getSalePercentage()) {
            throw new IllegalArgumentException("Total share exceeds sale percentage");
        }

        OwnerContract oc = OwnerContract.builder()
                .contract(contract)
                .user(user)
                .sharePercentage(req.getSharePercentage())
                .status(OwnerContractStatus.ACTIVE)
                .build();

        ownerContractRepository.save(oc);

        Ownership ownership = ownershipRepository
                .findByUser_UserIdAndVehicle_VehicleId(user.getId(), contract.getVehicle().getVehicleId())
                .orElse(Ownership.builder()
                        .user(user)
                        .vehicle(contract.getVehicle())
                        .totalSharePercentage(0.0)
                        .status(OwnershipStatus.ACTIVE)
                        .build());

        ownership.setTotalSharePercentage(ownership.getTotalSharePercentage() + req.getSharePercentage());
        ownershipRepository.save(ownership);

        return mapToResponse(oc);
    }

    private OwnerContractResponse mapToResponse(OwnerContract oc) {
        return OwnerContractResponse.builder()
                .ownerContractId(oc.getOwnerContractId())
                .userName(oc.getUser().getFullName())
                .sharePercentage(oc.getSharePercentage())
                .contractStatus(oc.getContract().getStatus().name())
                .createdAt(oc.getCreatedAt())
                .build();
    }
}
