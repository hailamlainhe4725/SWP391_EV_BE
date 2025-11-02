package com.example.demo.controller;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.enums.VerifyStatus;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

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

    
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/viewAllUser")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upLevelToStaff")
    public ResponseEntity<UserResponse> levelUpUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.phongStaff(email));
    
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{user_id}")
    public ResponseEntity<Void> delete(@PathVariable Long user_id) {
        userService.softDelete(user_id);
        return ResponseEntity.noContent().build();
    }

    //upload anh
    @PreAuthorize("hasAnyRole('USER','STAFF', 'ADMIN')")
@PostMapping(value = "/{id}/upload-documents", consumes = "multipart/form-data")
public ResponseEntity<UserResponse> uploadDocuments(
        @PathVariable Long id,
        @ModelAttribute UpdateUserDocumentRequest req) {
    return ResponseEntity.ok(userService.uploadUserDocuments(id, req));
}

//verify cho user
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{userId}/verify")
public ResponseEntity<UserResponse> verifyUser(
        @PathVariable Long userId,
        @RequestParam boolean approved,
        @RequestParam(required = false) String note) {
    return ResponseEntity.ok(userService.verifyUserDocuments(userId, approved, note));
}

// lay ra danh sach user chua verify
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/pending-verification")
public ResponseEntity<List<UserResponse>> getPendingUsers() {
    List<UserResponse> users = userService.getAll().stream()
            .filter(u -> u.getVerifyStatus() == VerifyStatus.PENDING)
            .collect(Collectors.toList());
    return ResponseEntity.ok(users);
}

}