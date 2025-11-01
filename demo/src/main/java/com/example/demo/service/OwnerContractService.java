package com.example.demo.service;

import com.example.demo.dto.request.CreateOwnerContractRequest;
import com.example.demo.dto.response.OwnerContractResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
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

        public List<OwnerContractResponse> getContractsByUser(Authentication authentication)  {
    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                if(!user.getVerified()) throw new RuntimeException("Account is not verify");
    List<OwnerContract> contracts = ownerContractRepository.findByUser_Id(user.getId());

    if (contracts.isEmpty()) {
        throw new RuntimeException("You have no owner contracts.");
    }

    return contracts.stream()
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

        double totalShareForVehicle = ownershipRepository.findByVehicle_VehicleId(contract.getVehicle().getVehicleId())
        .stream()
        .mapToDouble(Ownership::getTotalSharePercentage)
        .sum();

        if (totalShareForVehicle + req.getSharePercentage() > 100.0) {
        throw new IllegalArgumentException("Total vehicle ownership exceeds 100%");
        }


        OwnerContract oc = OwnerContract.builder()
                .contract(contract)
                .user(user)
                .sharePercentage(req.getSharePercentage())
                .status(OwnerContractStatus.ACTIVE)
                .build();

        ownerContractRepository.save(oc);

        // ===== Ownership update or create =====
        Ownership ownership = ownershipRepository
                .findByUser_IdAndVehicle_VehicleId(user.getId(), contract.getVehicle().getVehicleId())
                .orElse(Ownership.builder()
                        .user(user)
                        .vehicle(contract.getVehicle())
                        .totalSharePercentage(0.0)
                        .status(OwnershipStatus.ACTIVE)
                        .build());

        // Cập nhật tỷ lệ cổ phần
        ownership.setTotalSharePercentage(ownership.getTotalSharePercentage() + req.getSharePercentage());

        // ===== Tracking usage =====
        // Mỗi share = 30 ngày/năm = 2.5 ngày/tháng
        // Mỗi share = 3000 km/năm = 250 km/tháng
        double allowedDaysPerMonthPerShare = 30.0 / 12.0; // 2.5
        double allowedKmPerMonthPerShare = 3000.0 / 12.0; // 250

        double shareRatio = ownership.getTotalSharePercentage() / 10.0;

        ownership.setAllowedDaysThisMonth(allowedDaysPerMonthPerShare * shareRatio);
        ownership.setAllowedKmThisMonth(allowedKmPerMonthPerShare * shareRatio);

        // Giữ nguyên số đã dùng hoặc reset đầu tháng (tuỳ logic)
        if (ownership.getUsedDaysThisMonth() == null) ownership.setUsedDaysThisMonth(0.0);
        if (ownership.getUsedKmThisMonth() == null) ownership.setUsedKmThisMonth(0.0);

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
