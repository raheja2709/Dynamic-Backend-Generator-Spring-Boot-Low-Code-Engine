package com.user.driven.operations.app.api.dto;

/**
 * Response DTO returned upon successful authentication.
 *
 * @param accessToken  the JWT access token
 * @param refreshToken the JWT refresh token
 * @param email        the authenticated user's email
 * @param role         the user's role
 * @param expiresIn    access token expiration time in seconds
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String role,
        long expiresIn
) {}
