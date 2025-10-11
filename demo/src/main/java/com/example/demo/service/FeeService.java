package com.example.demo.service;

import com.example.demo.dto.response.FeeResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final VariableFeeRepository variableFeeRepository;
    private final FixedFeeRepository fixedFeeRepository;
    private final VehicleRepository vehicleRepository;

    // ========================= VARIABLE FEE =========================
    public List<FeeResponse> getVariableFeesByVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        return variableFeeRepository.findByVehicle(vehicle).stream()
                .filter(f -> !f.isDeleted())
                .map(this::mapFeeToResponse)
                .collect(Collectors.toList());
    }

    public FeeResponse createVariableFee(Long vehicleId, Long userId, VariableFee fee) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        fee.setVehicle(vehicle);
        if (userId != null) {
            fee.setUser(User.builder().id(userId).build());
        }
        fee.setDeleted(false);

        VariableFee saved = variableFeeRepository.save(fee);
        return mapFeeToResponse(saved);
    }

    public VariableFee updateVariableFee(Long id, VariableFee updatedFee) {
        VariableFee fee = variableFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variable fee not found"));

        fee.setType(updatedFee.getType());
        fee.setAmount(updatedFee.getAmount());
        fee.setDescription(updatedFee.getDescription());
        return variableFeeRepository.save(fee);
    }

    public void softDeleteVariableFee(Long id) {
        VariableFee fee = variableFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variable fee not found"));
        fee.setDeleted(true);
        variableFeeRepository.save(fee);
    }

    // ========================= FIXED FEE =========================
    public List<FeeResponse> getFixedFeesByVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        return fixedFeeRepository.findByVehicle(vehicle).stream()
                .filter(f -> !f.isDeleted())
                .map(this::mapFeeToResponse)
                .collect(Collectors.toList());
    }

    public FeeResponse createFixedFee(Long vehicleId, FixedFee fee) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        fee.setVehicle(vehicle);
        fee.setDeleted(false);
        FixedFee saved = fixedFeeRepository.save(fee);
        return mapFeeToResponse(saved);
    }

    public FixedFee updateFixedFee(Long id, FixedFee updatedFee) {
        FixedFee fee = fixedFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fixed fee not found"));

        fee.setType(updatedFee.getType());
        fee.setBaseAmount(updatedFee.getBaseAmount());
        fee.setDescription(updatedFee.getDescription());
        return fixedFeeRepository.save(fee);
    }

    public void softDeleteFixedFee(Long id) {
        FixedFee fee = fixedFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fixed fee not found"));
        fee.setDeleted(true);
        fixedFeeRepository.save(fee);
    }

    // ========================= MAPPER (Unified) =========================
    private FeeResponse mapFeeToResponse(Object f) {
        if (f instanceof VariableFee vf) {
            return FeeResponse.builder()
                    .feeId(vf.getVariableFeeId())
                    .vehicleId(vf.getVehicle().getVehicleId())
                    .userId(vf.getUser() != null ? vf.getUser().getId() : null)
                    .variableFeeType(vf.getType())
                    .amount(vf.getAmount())
                    .description(vf.getDescription())
                    .createdAt(vf.getCreatedAt())
                    .sourceType("Variable")
                    .build();

        } else if (f instanceof FixedFee ff) {
            return FeeResponse.builder()
                    .feeId(ff.getFixedFeeId())
                    .vehicleId(ff.getVehicle().getVehicleId())
                    .fixedFeeType(ff.getType())
                    .amount(ff.getBaseAmount())
                    .description(ff.getDescription())
                    .createdAt(ff.getCreatedAt())
                    .sourceType("Fixed")
                    .build();

        } else {
            throw new IllegalArgumentException("Unsupported fee type for mapping");
        }
    }
}
