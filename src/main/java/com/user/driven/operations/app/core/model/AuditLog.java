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
 * JPA entity representing an audit log entry for every API request.
 * Captures request metadata including method, endpoint, response status,
 * duration, client IP, user agent, and the generation stage if applicable.
 */
@Entity
@Table(name = "audit_logs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuditLog {

    /**
     * Unique identifier for the audit log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique request ID (UUID) correlating this log to a specific request.
     */
    @Column(name = "request_id", length = 36, nullable = false)
    private String requestId;

    /**
     * HTTP method (GET, POST, PUT, DELETE, etc.).
     */
    @Column(length = 10, nullable = false)
    private String method;

    /**
     * The request endpoint path, truncated to 500 characters max.
     */
    @Column(length = 500, nullable = false)
    private String endpoint;

    /**
     * The request body content, truncated to 10,000 characters if larger.
     */
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    /**
     * HTTP response status code.
     */
    @Column(name = "response_status")
    private Integer responseStatus;

    /**
     * Error message if the request resulted in an error.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Stack trace if an exception occurred during request processing.
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * The generation pipeline stage (e.g., VALIDATION, STRUCTURE, BUILD_VERIFICATION, ZIP).
     */
    @Column(length = 50)
    private String stage;

    /**
     * Duration of the request processing in milliseconds.
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * The client's IP address.
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * The client's User-Agent header value, truncated to 500 characters.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Timestamp when this audit log entry was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets the creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
