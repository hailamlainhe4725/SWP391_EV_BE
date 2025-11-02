package com.example.demo.service;

import com.example.demo.dto.request.CreateContractRequest;
import com.example.demo.dto.response.ContractResponse;
import com.example.demo.entity.Contract;
import com.example.demo.entity.FixedFee;
import com.example.demo.entity.OwnerContract;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.entity.Vehicle;
import com.example.demo.enums.ContractStatus;
import com.example.demo.enums.FixFeeType;
import com.example.demo.enums.OwnerContractStatus;
import com.example.demo.enums.OwnershipStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final OwnershipRepository ownershipRepository;

    private final OwnerContractRepository ownerContractRepository;
        private final StaffCheckingService checkingService;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final FixedFeeRepository fixedFeeRepository;


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

        public ContractResponse create(Authentication authentication,CreateContractRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        User admin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
                String userSignatureUrl = checkingService.uploadSignatureFile(req.getUserSignature(), "user");
                String adminSignatureUrl = checkingService.uploadSignatureFile(req.getAdminSignature(), "admin");

        // ===== 1. Tạo Contract =====
        Contract contract = Contract.builder()
                .user(user)
                .vehicle(vehicle)
                .salePercentage(req.getSalePercentage())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(req.getStatus() != null ? req.getStatus() : ContractStatus.PENDING)
                .admin(admin)
                .userSignatureUrl(userSignatureUrl)
                .adminSignatureUrl(adminSignatureUrl)
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
                createDefaultFixedFeesForVehicle(vehicle);
        }

        // ===== 4. Trả về response =====
        return mapToResponse(contract);
        }

        private void createDefaultFixedFeesForVehicle(Vehicle vehicle) {
    List<FixedFee> fees = List.of(
            FixedFee.builder()
                    .vehicle(vehicle)
                    .type(FixFeeType.Insurance)
                    .baseAmount(1_000_000.0)
                    .description("Monthly vehicle insurance")
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build(),
            FixedFee.builder()
                    .vehicle(vehicle)
                    .type(FixFeeType.Registration)
                    .baseAmount(130_000.0)
                    .description("Registration and road fee")
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build(),
            FixedFee.builder()
                    .vehicle(vehicle)
                    .type(FixFeeType.Maintenance)
                    .baseAmount(800_000.0)
                    .description("Periodic maintenance")
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build(),
            FixedFee.builder()
                    .vehicle(vehicle)
                    .type(FixFeeType.Cleaning)
                    .baseAmount(240_000.0)
                    .description("Car cleaning service")
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build(),
            FixedFee.builder()
                    .vehicle(vehicle)
                    .type(FixFeeType.OperationPerMonth)
                    .baseAmount(1_500_000.0)
                    .description("General monthly operation cost")
                    .createdAt(LocalDateTime.now())
                    .deleted(false)
                    .build()
    );

    fixedFeeRepository.saveAll(fees);
        }


 public List<ContractResponse> getContractsByUser(Authentication authentication) {
    // 1️⃣ Lấy thông tin user hiện tại
    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!user.getVerified()) {
        throw new RuntimeException("Account is not verified");
    }

    // 2️⃣ Lấy danh sách owner contracts mà user này tham gia
    List<OwnerContract> ownerContracts = ownerContractRepository.findByUser_Id(user.getId());
    if (ownerContracts.isEmpty()) {
        throw new RuntimeException("You have no owner contracts.");
    }

    // 3️⃣ Lấy danh sách contractId duy nhất (distinct)
    List<Long> uniqueContractIds = ownerContracts.stream()
            .map(oc -> oc.getContract().getContractId()) // lấy contractId
            .distinct() // loại bỏ trùng
            .collect(Collectors.toList());

    // 4️⃣ Từ danh sách contractId, lấy ra các Contract tương ứng
    List<Contract> contracts = contractRepository.findAllById(uniqueContractIds);

    // 5️⃣ Chuyển thành response DTO
    return contracts.stream()
            .map(this::mapToResponse) // dùng mapToResponse của bạn
            .collect(Collectors.toList());
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
                .user(c.getUser())
                .vehicle(c.getVehicle())
                .admin(c.getAdmin())
                .adminSignature(c.getAdminSignatureUrl())
                .userSignature(c.getUserSignatureUrl())
                .salePercentage(c.getSalePercentage())
                .status(c.getStatus().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
