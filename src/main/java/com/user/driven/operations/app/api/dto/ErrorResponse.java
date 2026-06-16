package com.user.driven.operations.app.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response returned by all exception handlers.
 * Provides consistent structure for API consumers to parse errors.
 *
 * @param timestamp   when the error occurred
 * @param status      HTTP status code
 * @param errorType   classification of the error (e.g., "NOT_FOUND", "VALIDATION_ERROR")
 * @param message     human-readable error message
 * @param path        the request path that caused the error
 * @param requestId   the X-Request-Id for tracing
 * @param fieldErrors field-level validation errors (present only for validation exceptions)
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorType,
        String message,
        String path,
        String requestId,
        List<FieldError> fieldErrors
) {

    /**
     * Represents a single field-level validation error.
     *
     * @param field         the field name that failed validation
     * @param message       the validation error message
     * @param rejectedValue the value that was rejected
     */
    public record FieldError(String field, String message, Object rejectedValue) {
    }
}
