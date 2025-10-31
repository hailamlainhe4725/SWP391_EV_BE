package com.example.demo.controller;

import com.example.demo.dto.request.CreateOwnerContractRequest;
import com.example.demo.dto.response.OwnerContractResponse;
import com.example.demo.service.OwnerContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/owner-contracts")
@RequiredArgsConstructor
public class OwnerContractController {

    private final OwnerContractService ownerContractService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/viewAllOwnerContract")
    public ResponseEntity<List<OwnerContractResponse>> getAll() {
        return ResponseEntity.ok(ownerContractService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createOwnerContract")
    public ResponseEntity<OwnerContractResponse> create(@RequestBody CreateOwnerContractRequest req) {
        return ResponseEntity.ok(ownerContractService.create(req));
    }

        @PreAuthorize("hasRole( 'ADMIN')")
    @GetMapping("/viewMyOwnerContract")
    public ResponseEntity<List<OwnerContractResponse>> getMyContracts(Authentication authentication) {
        return ResponseEntity.ok(ownerContractService.getContractsByUser(authentication));
    }
}
