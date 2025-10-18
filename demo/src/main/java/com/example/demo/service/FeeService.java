package com.example.demo.service;

import com.example.demo.dto.request.CreateFixedFeeRequest;
import com.example.demo.dto.request.CreateVariableFeeRequest;
import com.example.demo.dto.response.FeeResponse;
import com.example.demo.entity.FixedFee;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.entity.VariableFee;
import com.example.demo.entity.Vehicle;
import com.example.demo.enums.FixFeeType;
import com.example.demo.enums.VariableFeeType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.FixedFeeRepository;
import com.example.demo.repository.OwnershipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VariableFeeRepository;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeService {
        private final OwnershipRepository ownershipRepository;
        private final VariableFeeRepository variableFeeRepository;
        private final FixedFeeRepository fixedFeeRepository;
        private final VehicleRepository vehicleRepository;
        private final UserRepository userRepository;

        // ================= VARIABLE FEE =================
        public FeeResponse createVariableFee(CreateVariableFeeRequest req) {
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

                User user = userRepository.findByEmail(req.getEmail())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Ownership ownership = ownershipRepository.findByUser_IdAndVehicle_VehicleId(
                                user.getId(), vehicle.getVehicleId())
                                .orElseThrow(() -> new RuntimeException("Ownership not found for this vehicle"));                
                VariableFee fee = VariableFee.builder()
                                .vehicle(vehicle)
                                .user(user)
                                .type(req.getType())
                                .amount(req.getAmount())
                                .description(req.getDescription())
                                .createdAt(LocalDateTime.now())
                                .deleted(false)
                                .build();

                variableFeeRepository.save(fee);
                return mapFeeToResponse(fee);
        }

        public List<FeeResponse> getAllVariableFees() {
                return variableFeeRepository.findByDeletedFalse()
                                .stream().map(this::mapFeeToResponse).collect(Collectors.toList());
        }

        // ================= FIXED FEE =================
        public FeeResponse createFixedFee(CreateFixedFeeRequest req) {
                Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

                FixedFee fee = FixedFee.builder()
                                .vehicle(vehicle)
                                .type(req.getType())
                                .baseAmount(req.getBaseAmount())
                                .description(req.getDescription())
                                .createdAt(LocalDateTime.now())
                                .deleted(false)
                                .build();

                fixedFeeRepository.save(fee);
                return mapFeeToResponse(fee);
        }

        public List<FeeResponse> getAllFixedFees() {
                return fixedFeeRepository.findByDeletedFalse()
                                .stream().map(this::mapFeeToResponse).collect(Collectors.toList());
        }

        // ================= MAPPER =================
        private FeeResponse mapFeeToResponse(Object fee) {
                if (fee instanceof VariableFee vf) {
                        return FeeResponse.builder()
                                        .feeId(vf.getVariableFeeId())
                                        .sourceType("Variable")
                                        .variableFeeType(vf.getType())
                                        .vehicleId(vf.getVehicle().getVehicleId())
                                        .userId(vf.getUser().getId())
                                        .amount(vf.getAmount())
                                        .description(vf.getDescription())
                                        .createdAt(vf.getCreatedAt())
                                        .build();
                } else if (fee instanceof FixedFee ff) {
                        return FeeResponse.builder()
                                        .feeId(ff.getFixedFeeId())
                                        .sourceType("Fixed")
                                        .fixedFeeType(ff.getType())
                                        .vehicleId(ff.getVehicle().getVehicleId())
                                        .amount(ff.getBaseAmount())
                                        .description(ff.getDescription())
                                        .createdAt(ff.getCreatedAt())
                                        .build();
                }
                throw new IllegalArgumentException("Unsupported fee type");
        }
}
