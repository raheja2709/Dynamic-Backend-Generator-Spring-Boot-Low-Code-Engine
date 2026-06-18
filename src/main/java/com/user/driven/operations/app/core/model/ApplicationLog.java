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
 * JPA entity representing an application log entry for ERROR and WARN level events.
 * Captures log metadata including level, logger name, message, stack trace,
 * request ID for correlation, user ID, and optional JSONB context.
 */
@Entity
@Table(name = "application_logs")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApplicationLog {

    /**
     * Unique identifier for the application log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Log level (ERROR, WARN).
     */
    @Column(length = 10, nullable = false)
    private String level;

    /**
     * Fully qualified logger name that produced the log event.
     */
    @Column(name = "logger_name", length = 255, nullable = false)
    private String loggerName;

    /**
     * The formatted log message.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Stack trace if the log event included a throwable.
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * The request ID (UUID) correlating this log to a specific request.
     * Retrieved from MDC context set by RequestIdFilter.
     */
    @Column(name = "request_id", length = 36)
    private String requestId;

    /**
     * The ID of the user who triggered the request (if authenticated).
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Optional JSON context providing additional structured metadata about the log event.
     * Stored as TEXT for H2 compatibility; use JSONB column type for PostgreSQL.
     */
    @Column(columnDefinition = "TEXT")
    private String context;

    /**
     * Timestamp when this application log entry was created.
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
