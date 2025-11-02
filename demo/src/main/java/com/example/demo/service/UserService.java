package com.example.demo.service;

import com.example.demo.dto.request.CreateUserRequest;
import com.example.demo.dto.request.UpdateUserDocumentRequest;
import com.example.demo.dto.request.UpdateUserRequest;
import com.example.demo.dto.request.AuthRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import com.example.demo.enums.VerifyStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
private AuthenticationManager authManager;
@Autowired
private CustomUserDetailsService userDetailsService;
@Autowired
private JwtUtils jwtUtils;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_UPLOAD_DIR = "uploads/users/";

    // ===== Get all =====
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===== Get by id =====
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    // ===== Get by email =====
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    // ===== Create =====
    public UserResponse create(CreateUserRequest req) {
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(req.getPassword());
        user.setVerifyStatus(VerifyStatus.PENDING);

        // Enum mapping
        String roleStr = (req.getRole() == null || req.getRole().isEmpty()) ? "USER" : req.getRole().toUpperCase();

        try {
            user.setRole(UserRole.valueOf(roleStr)); // convert String -> Enum
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + req.getRole());
        }
        userRepository.save(user);
        return mapToResponse(user);
    }




    // ===== Update =====
    public UserResponse update(String email, UpdateUserRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (req.getFullName() != null)
            user.setFullName(req.getFullName());
        if (req.getPhone() != null)
            user.setPhone(req.getPhone());

        userRepository.save(user);
        return mapToResponse(user);
    }
    public UserResponse uploadUserDocuments(Long userId, UpdateUserDocumentRequest req) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            if(user.getVerified()) throw new RuntimeException("CCCD AND GPLX is verified");
            Files.createDirectories(Paths.get(USER_UPLOAD_DIR));

            // --- cập nhật số CCCD, GPLX ---
            if (req.getCccd() != null) user.setCccd(req.getCccd());
            if (req.getGplx() != null) user.setGplx(req.getGplx());

            // --- xử lý file ảnh CCCD ---
            MultipartFile cccdFile = req.getCccdFile();
            if (cccdFile != null && !cccdFile.isEmpty()) {
                String cccdFileName = "cccd_" + userId + "_" + cccdFile.getOriginalFilename();
                Path cccdPath = Paths.get(USER_UPLOAD_DIR + cccdFileName);
                cccdFile.transferTo(cccdPath);
                user.setCccdImagePath(cccdPath.toString());
            }

            // --- xử lý file ảnh GPLX ---
            MultipartFile gplxFile = req.getGplxFile();
            if (gplxFile != null && !gplxFile.isEmpty()) {
                String gplxFileName = "gplx_" + userId + "_" + gplxFile.getOriginalFilename();
                Path gplxPath = Paths.get(USER_UPLOAD_DIR + gplxFileName);
                gplxFile.transferTo(gplxPath);
                user.setGplxImagePath(gplxPath.toString());
            }

            userRepository.save(user);
            return mapToResponse(user);

        } catch (IOException e) {
            throw new RuntimeException("Error saving user documents: " + e.getMessage());
        }
    }
    public UserResponse verifyUserDocuments(Long userId, boolean approved, String note) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (approved) {
        user.setVerified(true);
        user.setVerifyStatus(VerifyStatus.APPROVED);
        user.setVerifyNote(note != null ? note : "Documents verified successfully");
    } else {
        user.setVerified(false);
        user.setVerifyStatus(VerifyStatus.REJECTED);
        user.setVerifyNote(note != null ? note : "Documents rejected");
    }

    userRepository.save(user);
    return mapToResponse(user);
}

    // ===== Soft delete =====
    public void softDelete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setDeleted(true);
        userRepository.save(user);
    }

    // ===== Authenticate (mock for now) =====
public AuthResponse authenticate(AuthRequest req) {
    authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
    );
    var user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));
    var userDetails = userDetailsService.loadUserByUsername(req.getEmail());
    String token = jwtUtils.generateToken(userDetails);
    return new AuthResponse(token, user.getRole().name(), user.getFullName());
}

    // ===== Mapper =====
    private UserResponse mapToResponse(User user) {
    return UserResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .role(user.getRole().name())
            .cccd(user.getCccd())
            .gplx(user.getGplx())
            .cccdImagePath(user.getCccdImagePath())
            .gplxImagePath(user.getGplxImagePath())
            .verified(user.getVerified())
            .verifyStatus(user.getVerifyStatus())
            .verifyNote(user.getVerifyNote())
            .build();
}

}
