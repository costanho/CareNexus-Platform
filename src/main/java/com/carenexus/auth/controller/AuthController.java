package com.carenexus.auth.controller;

import com.carenexus.auth.dto.LoginRequest;
import com.carenexus.auth.dto.AuthResponse;
import com.carenexus.auth.dto.RefreshTokenRequest;
import com.carenexus.auth.dto.UserInfoResponse;
import com.carenexus.auth.model.User;
import com.carenexus.auth.repository.UserRepository;
import com.carenexus.auth.security.JwtService;
import com.carenexus.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /** 🔹 Register a new user */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) {
        AuthResponse response = authService.register(user);
        return ResponseEntity.ok(response);
    }

    /** 🔹 Login & return tokens */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    /** 🔹 Refresh access token */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /** 🔒 Protected test endpoint */
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Protected endpoint accessed!");
    }

    /** 🔒 Get currently authenticated user info */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(response);
    }

}
