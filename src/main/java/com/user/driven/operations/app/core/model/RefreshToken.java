package com.user.driven.operations.app.core.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a refresh token issued to a user for obtaining new JWT access tokens.
 * Tokens can be revoked and have an expiration time.
 *
 * @author Jatin Raheja
 */
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshToken {

    /**
     * Unique identifier for the refresh token record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The token string value. Must be unique and not null.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * The ID of the user this token belongs to.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * When this token expires.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Whether this token has been revoked.
     */
    @Column(name = "revoked")
    private boolean revoked = false;

    /**
     * Timestamp for when the token was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets the creation timestamp before the token is persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
