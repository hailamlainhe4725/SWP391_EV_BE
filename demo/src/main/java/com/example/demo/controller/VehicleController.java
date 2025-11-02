package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/top4")
    public ResponseEntity<List<VehicleResponse>> getTop4Vehicle() {
        return ResponseEntity.ok(vehicleService.gettop4());
    }

    @GetMapping("/viewAllVehicle")
    public ResponseEntity<List<VehicleResponse>> getAll() {
        return ResponseEntity.ok(vehicleService.getAll());
    }
    
    @PreAuthorize("hasAnyRole('USER','STAFF','ADMIN')")
    @GetMapping("/vehicle/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping(value = "/createVehicle", consumes = "multipart/form-data")
public ResponseEntity<VehicleResponse> create(@Valid @ModelAttribute CreateVehicleRequest req) {
    return ResponseEntity.ok(vehicleService.create(req));
}


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateVehicleRequest req) {
        return ResponseEntity.ok(vehicleService.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
