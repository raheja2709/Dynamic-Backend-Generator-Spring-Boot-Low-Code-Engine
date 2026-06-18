package com.user.driven.operations.app.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom Logback appender that persists ERROR and WARN log events
 * directly to the application_logs table using JDBC.
 * <p>
 * Uses direct JDBC (bypassing JPA/Hibernate) to avoid circular logging
 * dependencies — Hibernate itself logs, which would cause infinite recursion
 * if we used JPA for log persistence within the logging framework.
 * <p>
 * Gracefully handles all failures without interrupting the application.
 * If the DataSource is not yet available (during startup), log events are silently skipped.
 *
 * @see com.user.driven.operations.app.core.model.ApplicationLog
 */
public class DatabaseLogAppender extends AppenderBase<ILoggingEvent> {

    private static final String INSERT_SQL =
            "INSERT INTO application_logs (level, logger_name, message, stack_trace, request_id, user_id, context, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private volatile DataSource dataSource;

    /**
     * Sets the DataSource used for persisting log entries.
     * Called by {@link DatabaseLogAppenderInitializer} once the Spring context is ready.
     *
     * @param dataSource the application DataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void append(ILoggingEvent event) {
        // Only persist WARN and ERROR level events
        if (event.getLevel().levelInt < Level.WARN.levelInt) {
            return;
        }

        // DataSource not yet available during application startup
        if (dataSource == null) {
            return;
        }

        // Skip logging from Hibernate/JDBC/HikariCP to avoid infinite recursion
        String loggerName = event.getLoggerName();
        if (isInfrastructureLogger(loggerName)) {
            return;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, event.getLevel().toString());
            ps.setString(2, loggerName);
            ps.setString(3, event.getFormattedMessage());

            // Stack trace from throwable proxy
            IThrowableProxy throwableProxy = event.getThrowableProxy();
            ps.setString(4, throwableProxy != null ? ThrowableProxyUtil.asString(throwableProxy) : null);

            // Request ID from MDC (set by RequestIdFilter)
            Map<String, String> mdcMap = event.getMDCPropertyMap();
            ps.setString(5, mdcMap != null ? mdcMap.get("requestId") : null);

            // User ID from MDC (will be set by security filters in future tasks)
            String userIdStr = mdcMap != null ? mdcMap.get("userId") : null;
            if (userIdStr != null) {
                try {
                    ps.setLong(6, Long.parseLong(userIdStr));
                } catch (NumberFormatException e) {
                    ps.setNull(6, java.sql.Types.BIGINT);
                }
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }

            // Context from MDC (optional JSON context)
            ps.setString(7, mdcMap != null ? mdcMap.get("logContext") : null);

            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
        } catch (Exception e) {
            // Requirement 5.7: Gracefully handle failures — log to console, don't interrupt request
            addError("Failed to persist log entry to database", e);
        }
    }

    /**
     * Checks if the logger belongs to infrastructure components that would cause
     * infinite recursion if their logs were persisted to the database.
     */
    private boolean isInfrastructureLogger(String loggerName) {
        return loggerName.startsWith("org.hibernate")
                || loggerName.startsWith("org.springframework.jdbc")
                || loggerName.startsWith("com.zaxxer.hikari")
                || loggerName.startsWith("org.springframework.orm")
                || loggerName.startsWith("org.springframework.transaction")
                || loggerName.startsWith("org.flywaydb")
                || loggerName.startsWith("com.user.driven.operations.app.common.logging");
    }
}
