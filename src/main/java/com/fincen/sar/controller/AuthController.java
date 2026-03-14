package com.fincen.sar.controller;

import com.fincen.sar.dto.AuthRequest;
import com.fincen.sar.dto.AuthResponse;
import com.fincen.sar.dto.RegisterRequest;
import com.fincen.sar.entity.AppUser;
import com.fincen.sar.entity.Role;
import com.fincen.sar.exception.ConflictException;
import com.fincen.sar.repository.AppUserRepository;
import com.fincen.sar.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and user registration")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final AppUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Authenticate and receive JWT token")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        AppUser user = userRepo.findByUsername(req.getUsername())
                .orElseThrow();

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    @Operation(summary = "Register a new user (ADMIN only)")
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new ConflictException("Username already exists: " + req.getUsername());
        }

        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + req.getRole()
                    + ". Must be one of: ANALYST, REVIEWER, APPROVER, ADMIN");
        }

        AppUser user = AppUser.builder()
                .username(req.getUsername())
                .fullName(req.getFullName())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .build();

        userRepo.save(user);

        return AuthResponse.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}
