package com.example.demo.controller;

import com.example.demo.dto.response.FeeResponse;
import com.example.demo.entity.FixedFee;
import com.example.demo.entity.VariableFee;
import com.example.demo.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    // ===== Variable Fees =====
    @GetMapping("/variable/{vehicleId}")
    public ResponseEntity<List<FeeResponse>> getVariableFees(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(feeService.getVariableFeesByVehicle(vehicleId));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/variable/{vehicleId}/{userId}")
    public ResponseEntity<FeeResponse> createVariable(@PathVariable Long vehicleId, @PathVariable Long userId,
            @RequestBody VariableFee fee) {
        return ResponseEntity.ok(feeService.createVariableFee(vehicleId, userId, fee));
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/variable/{id}")
    public ResponseEntity<Void> deleteVariable(@PathVariable Long id) {
        feeService.softDeleteVariableFee(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Fixed Fees =====
    @GetMapping("/fixed/{vehicleId}")
    public ResponseEntity<List<FeeResponse>> getFixedFees(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(feeService.getFixedFeesByVehicle(vehicleId));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/fixed/{vehicleId}")
    public ResponseEntity<FeeResponse> createFixed(@PathVariable Long vehicleId, @RequestBody FixedFee fee) {
        return ResponseEntity.ok(feeService.createFixedFee(vehicleId, fee));
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/fixed/{id}")
    public ResponseEntity<Void> deleteFixed(@PathVariable Long id) {
        feeService.softDeleteFixedFee(id);
        return ResponseEntity.noContent().build();
    }
}
