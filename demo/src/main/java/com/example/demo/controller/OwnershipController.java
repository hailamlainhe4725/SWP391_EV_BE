package com.example.demo.controller;

import com.example.demo.dto.response.OwnershipResponse;
import com.example.demo.dto.response.OwnershipVehicleResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.entity.Ownership;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.OwnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ownerships")
@RequiredArgsConstructor
public class OwnershipController {

    private final OwnershipService ownershipService;
// tim theo id 
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/viewMyOnwership")
    public ResponseEntity<List<OwnershipResponse>> getMyOwnerships(Authentication auth) {
        return ResponseEntity.ok(ownershipService.getByUserEmail(auth.getName()));
    }

    @PreAuthorize("hasRole('STAFF','ADMIN')")
    @GetMapping("/viewAllOwnership")
    public ResponseEntity<List<OwnershipResponse>> getAll() {
        return ResponseEntity.ok(ownershipService.getAll());
    }

    @GetMapping("/my-vehicles")
@PreAuthorize("hasRole('USER','ADMIN')")
public ResponseEntity<List<VehicleResponse>> getMyVehicles(Authentication auth) {
    return ResponseEntity.ok(ownershipService.getVehicleInMyOwnership(auth));
}

    @GetMapping("/viewMygroupOwnership/{vehicle_Id}")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public ResponseEntity<List<OwnershipResponse>> getMyGroup(Authentication auth,@PathVariable Long vehicle_Id) {
    return ResponseEntity.ok(ownershipService.getGroupOwnership(auth,vehicle_Id));
}

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/viewVehicleAndOwnship")
public List<OwnershipVehicleResponse> getMyOwnershipVehicles(Authentication authentication) {
        return ownershipService.getMyOwnershipVehicles(authentication);
    }

}
