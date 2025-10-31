package com.example.demo.controller;

import com.example.demo.dto.request.CreateStaffCheckingRequest;
import com.example.demo.dto.request.StaffCheckingConfirmRequest;
import com.example.demo.dto.response.StaffCheckingResponse;
import com.example.demo.service.StaffCheckingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff-checkings")
@RequiredArgsConstructor
public class StaffCheckingController {

    private final StaffCheckingService staffCheckingService;

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/viewAllStaffChecking")
    public ResponseEntity<List<StaffCheckingResponse>> getAll() {
        return ResponseEntity.ok(staffCheckingService.getAll());
    }

    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<StaffCheckingResponse>> getByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(staffCheckingService.getByBooking(bookingId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/viewAllMyBooking")
     public ResponseEntity<List<StaffCheckingResponse>> getByBooking(Authentication authentication) {
        return ResponseEntity.ok(staffCheckingService.getByBookingAuthentication(authentication));
    }


    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping(value = "/createStaffChecking", consumes = "multipart/form-data")
    
    public ResponseEntity<StaffCheckingResponse> create(Authentication authentication,@ModelAttribute CreateStaffCheckingRequest req) {
        return ResponseEntity.ok(staffCheckingService.create(authentication,req));
    }
    @PreAuthorize("hasRole('USER')")
@PostMapping(value ="/{id}/confirm",consumes = "multipart/form-data")
public ResponseEntity<StaffCheckingResponse> confirm(
        Authentication authentication,
        @PathVariable Long id,
        @ModelAttribute StaffCheckingConfirmRequest req
) {
    return ResponseEntity.ok(staffCheckingService.confirm(authentication, id, req));
}

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffCheckingService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
