package com.user.driven.operations.app.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user authentication (login).
 *
 * @param email    the user's email address (must be valid and non-blank)
 * @param password the user's password (must be non-blank)
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
