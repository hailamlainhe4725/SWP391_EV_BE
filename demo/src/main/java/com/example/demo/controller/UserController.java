package com.example.demo.controller;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/login") // login endpoint (returns token via AuthResponse)
    public ResponseEntity<AuthResponse> auth(@Valid @RequestBody AuthRequest request) {
        AuthResponse res = userService.authenticate(request);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN','STAFF')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        String email = auth.getName();
        UserResponse res = userService.getByEmail(email);
        return ResponseEntity.ok(res);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<UserResponse> update(
            @Valid @RequestBody UpdateUserRequest req,
            Authentication auth) {
        // optional: allow self or staff
        UserResponse updated = userService.update(auth.getName(), req);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/viewAllUser")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{user_id}")
    public ResponseEntity<Void> delete(@PathVariable Long user_id) {
        userService.softDelete(user_id);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasAnyRole('USER','STAFF', 'ADMIN')")
@PostMapping(value = "/{id}/upload-documents", consumes = "multipart/form-data")
public ResponseEntity<UserResponse> uploadDocuments(
        @PathVariable Long id,
        @ModelAttribute UpdateUserDocumentRequest req) {
    return ResponseEntity.ok(userService.uploadUserDocuments(id, req));
}

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{userId}/verify")
public ResponseEntity<UserResponse> verifyUser(
        @PathVariable Long userId,
        @RequestParam boolean approved,
        @RequestParam(required = false) String note) {
    return ResponseEntity.ok(userService.verifyUserDocuments(userId, approved, note));
}

}