package com.example.demo.controller;

import com.example.demo.dto.request.CreateOwnershipRequest;
import com.example.demo.dto.response.OwnershipResponse;
import com.example.demo.service.OwnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ownerships")
@RequiredArgsConstructor
public class OwnershipController {

    private final OwnershipService ownershipService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ResponseEntity<List<OwnershipResponse>> getMyOwnerships(Authentication auth) {
        String email = auth.getName();
        List<OwnershipResponse> res = ownershipService.getByUserEmail(email);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping
    public ResponseEntity<List<OwnershipResponse>> getAll() {
        return ResponseEntity.ok(ownershipService.getAll());
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    public ResponseEntity<OwnershipResponse> create(@RequestBody CreateOwnershipRequest req) {
        return ResponseEntity.ok(ownershipService.create(req));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<OwnershipResponse> update(@PathVariable Long id, @RequestBody CreateOwnershipRequest req) {
        return ResponseEntity.ok(ownershipService.update(id, req));
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ownershipService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
