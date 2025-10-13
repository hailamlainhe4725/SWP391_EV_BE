package com.example.demo.controller;

import com.example.demo.dto.request.CreateFixedFeeRequest;
import com.example.demo.dto.request.CreateVariableFeeRequest;
import com.example.demo.dto.response.FeeResponse;
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

    // === STAFF: tạo phí biến động ===
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/variable")
    public ResponseEntity<FeeResponse> createVariableFee(@RequestBody CreateVariableFeeRequest req) {
        return ResponseEntity.ok(feeService.createVariableFee(req));
    }

    // === STAFF: tạo phí cố định ===
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/fixed")
    public ResponseEntity<FeeResponse> createFixedFee(@RequestBody CreateFixedFeeRequest req) {
        return ResponseEntity.ok(feeService.createFixedFee(req));
    }

    // === STAFF: xem tất cả phí ===
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/all")
    public ResponseEntity<List<FeeResponse>> getAllFees() {
        List<FeeResponse> res = feeService.getAllVariableFees();
        res.addAll(feeService.getAllFixedFees());
        return ResponseEntity.ok(res);
    }

    // === USER: xem phí theo xe ===
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<FeeResponse>> getFeesByVehicle(@PathVariable Long vehicleId) {
        List<FeeResponse> res = feeService.getAllVariableFees().stream()
                .filter(f -> f.getVehicleId().equals(vehicleId))
                .toList();
        return ResponseEntity.ok(res);
    }
}
