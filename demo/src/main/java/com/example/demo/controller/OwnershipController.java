package com.example.demo.controller;

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
    @GetMapping("/viewMyOnwership")
    public ResponseEntity<List<OwnershipResponse>> getMyOwnerships(Authentication auth) {
        return ResponseEntity.ok(ownershipService.getByUserEmail(auth.getName()));
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/viewAllOwnership")
    public ResponseEntity<List<OwnershipResponse>> getAll() {
        return ResponseEntity.ok(ownershipService.getAll());
    }
}
