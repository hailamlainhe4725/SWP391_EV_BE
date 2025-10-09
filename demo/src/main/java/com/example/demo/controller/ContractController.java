package com.example.demo.controller;

import com.example.demo.dto.request.CreateContractRequest;
import com.example.demo.dto.response.ContractResponse;
import com.example.demo.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PreAuthorize("hasAnyRole('USER','STAFF')")
    @GetMapping("/my")
    public ResponseEntity<List<ContractResponse>> getMyContracts(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(contractService.getByUserEmail(email));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    public ResponseEntity<ContractResponse> create(@RequestBody CreateContractRequest req) {
        return ResponseEntity.ok(contractService.create(req));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ContractResponse> update(@PathVariable Long id, @RequestBody CreateContractRequest req) {
        return ResponseEntity.ok(contractService.update(id, req));
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
