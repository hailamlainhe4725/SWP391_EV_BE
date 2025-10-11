package com.example.demo.controller;

import com.example.demo.dto.request.CreateContractRequest;
import com.example.demo.dto.response.ContractResponse;
import com.example.demo.enums.ContractStatus;
import com.example.demo.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    public ResponseEntity<ContractResponse> create(@RequestBody CreateContractRequest req) {
        return ResponseEntity.ok(contractService.create(req));
    }

    @PreAuthorize("hasAnyRole('USER','STAFF')")
    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAll() {
        return ResponseEntity.ok(contractService.getAll());
    }

    @PreAuthorize("hasAnyRole('USER','STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ContractResponse> updateStatus(@PathVariable Long id, @RequestParam ContractStatus status) {
        return ResponseEntity.ok(contractService.updateStatus(id, status));
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
