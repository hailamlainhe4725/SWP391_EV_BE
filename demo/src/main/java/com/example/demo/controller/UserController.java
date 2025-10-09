package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest req) {
        UserResponse created = userService.create(req);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/auth") // login endpoint (returns token via AuthResponse)
    public ResponseEntity<AuthResponse> auth(@Valid @RequestBody AuthRequest request) {
        AuthResponse res = userService.authenticate(request);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasAnyRole('USER','STAFF')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        String email = auth.getName();
        UserResponse res = userService.getByEmail(email);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasAnyRole('USER','STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest req,
            Authentication auth) {
        // optional: allow self or staff
        UserResponse updated = userService.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}