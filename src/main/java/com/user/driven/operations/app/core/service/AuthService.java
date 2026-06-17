package com.user.driven.operations.app.core.service;

import com.user.driven.operations.app.api.dto.AuthResponse;
import com.user.driven.operations.app.api.dto.LoginRequest;
import com.user.driven.operations.app.api.dto.RegisterRequest;
import com.user.driven.operations.app.api.dto.UserResponse;

/**
 * Service interface for authentication operations including user registration and login.
 *
 * @author Jatin Raheja
 */
public interface AuthService {

    /**
     * Registers a new user with validated credentials.
     * Password is validated for strength rules and BCrypt-hashed before storage.
     *
     * @param request the registration request containing email, password, and optional full name
     * @return the created user details (excluding password hash)
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates a user with email and password credentials.
     * Returns JWT access and refresh tokens upon successful authentication.
     *
     * @param request the login request containing email and password
     * @return authentication response with tokens and user details
     */
    AuthResponse login(LoginRequest request);
}
