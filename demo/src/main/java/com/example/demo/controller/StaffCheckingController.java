package com.example.demo.controller;

import com.example.demo.dto.request.CreateStaffCheckingRequest;
import com.example.demo.dto.response.StaffCheckingResponse;
import com.example.demo.service.StaffCheckingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff-checkings")
@RequiredArgsConstructor
public class StaffCheckingController {

    private final StaffCheckingService staffCheckingService;

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/viewAllStaffChecking")
    public ResponseEntity<List<StaffCheckingResponse>> getAll() {
        return ResponseEntity.ok(staffCheckingService.getAll());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<StaffCheckingResponse>> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(staffCheckingService.getByBooking(bookingId));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/createStaffChecking")
    public ResponseEntity<StaffCheckingResponse> create(@RequestBody CreateStaffCheckingRequest req) {
        return ResponseEntity.ok(staffCheckingService.create(req));
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffCheckingService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
