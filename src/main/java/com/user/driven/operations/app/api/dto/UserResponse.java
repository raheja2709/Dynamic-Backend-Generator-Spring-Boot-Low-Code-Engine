package com.user.driven.operations.app.api.dto;

import java.time.LocalDateTime;

/**
 * Response DTO representing user details (excludes password hash).
 *
 * @param id        the user's unique identifier
 * @param email     the user's email address
 * @param fullName  the user's full name
 * @param role      the user's role
 * @param createdAt when the user account was created
 */
public record UserResponse(
        Long id,
        String email,
        String fullName,
        String role,
        LocalDateTime createdAt
) {}
