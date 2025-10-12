package com.example.demo.controller;

import com.example.demo.dto.request.CreateFixedFeeRequest;
import com.example.demo.dto.request.CreateVariableFeeRequest;
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

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/variable")
    public ResponseEntity<FeeResponse> createVariable(@RequestBody CreateVariableFeeRequest req) {
        return ResponseEntity.ok(feeService.createVariableFee(req));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/fixed")
    public ResponseEntity<FeeResponse> createFixed(@RequestBody CreateFixedFeeRequest req) {
        return ResponseEntity.ok(feeService.createFixedFee(req));
    }
}
