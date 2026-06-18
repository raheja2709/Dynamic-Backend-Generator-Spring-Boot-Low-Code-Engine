package com.user.driven.operations.app.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user registration.
 *
 * @param email    the user's email address (must be valid and non-blank)
 * @param password the user's password (validated by AuthService for strength rules)
 * @param fullName the user's full name (optional)
 */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String fullName
) {}
