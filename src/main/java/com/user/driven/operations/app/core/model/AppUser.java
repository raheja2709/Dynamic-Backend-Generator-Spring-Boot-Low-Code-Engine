package com.user.driven.operations.app.core.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an application user for Host API authentication and authorization.
 * Users can authenticate via JWT tokens or API keys and have role-based access control.
 *
 * @author Jatin Raheja
 */
@Entity
@Table(name = "app_users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppUser {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's email address. Must be unique and not null.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt-hashed password.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * User's full name.
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * User role (USER or ADMIN). Defaults to USER.
     */
    @Column(name = "role", nullable = false, length = 50)
    private String role = "USER";

    /**
     * Whether the user account is enabled.
     */
    @Column(name = "enabled")
    private boolean enabled = true;

    /**
     * Whether the user account is locked.
     */
    @Column(name = "locked")
    private boolean locked = false;

    /**
     * API key for programmatic access. Must be unique if set.
     */
    @Column(name = "api_key", unique = true)
    private String apiKey;

    /**
     * Timestamp for when the user was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp for when the user was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Timestamp for the user's last login.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * Automatically sets timestamps before the user is persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically updates the timestamp when the user is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
