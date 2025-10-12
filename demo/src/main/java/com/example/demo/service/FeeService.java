package com.example.demo.service;

import com.example.demo.dto.request.CreateFixedFeeRequest;
import com.example.demo.dto.request.CreateVariableFeeRequest;
import com.example.demo.dto.response.FeeResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeService {
    private final VariableFeeRepository variableFeeRepository;
    private final FixedFeeRepository fixedFeeRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    // === VARIABLE ===
    public FeeResponse createVariableFee(CreateVariableFeeRequest req) {
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        VariableFee vf = VariableFee.builder()
                .vehicle(vehicle)
                .user(user)
                .type(req.getType())
                .amount(req.getAmount())
                .description(req.getDescription())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        variableFeeRepository.save(vf);
        return mapToResponse(vf);
    }

    // === FIXED ===
    public FeeResponse createFixedFee(CreateFixedFeeRequest req) {
        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        FixedFee ff = FixedFee.builder()
                .vehicle(vehicle)
                .type(req.getType())
                .baseAmount(req.getBaseAmount())
                .frequency(req.getFrequency())
                .description(req.getDescription())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        fixedFeeRepository.save(ff);
        return mapToResponse(ff);
    }

    // === MAPPING ===
    private FeeResponse mapToResponse(Object fee) {
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
        } else
            throw new RuntimeException("Unsupported fee type");
    }
}
