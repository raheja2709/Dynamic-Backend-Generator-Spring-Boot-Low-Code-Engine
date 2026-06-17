package com.user.driven.operations.app.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.driven.operations.app.api.dto.AuthResponse;
import com.user.driven.operations.app.api.dto.LoginRequest;
import com.user.driven.operations.app.api.dto.RegisterRequest;
import com.user.driven.operations.app.api.dto.UserResponse;
import com.user.driven.operations.app.core.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user registration and login.
 *
 * @author Jatin Raheja
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     * Validates password strength rules and creates the user with a default USER role.
     *
     * @param request the registration request containing email, password, and optional full name
     * @return the created user details with HTTP 201
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with validated credentials and default USER role")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or password validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns JWT tokens.
     * Validates credentials and returns access and refresh tokens upon success.
     *
     * @param request the login request containing email and password
     * @return authentication response with tokens and user details
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get tokens", description = "Validates credentials and returns JWT access and refresh tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
