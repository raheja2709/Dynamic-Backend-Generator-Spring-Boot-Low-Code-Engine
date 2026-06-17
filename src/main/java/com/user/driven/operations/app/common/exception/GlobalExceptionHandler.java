package com.user.driven.operations.app.common.exception;

import com.user.driven.operations.app.api.dto.ErrorResponse;
import com.user.driven.operations.app.common.util.MessageConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Global exception handler that converts exceptions into consistent {@link ErrorResponse} payloads.
 * All responses include the X-Request-Id from MDC for request tracing.
 *
 * @author Jatin Raheja
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * Handles Spring Bean Validation failures from @Valid-annotated request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation failed for request {}: {}", getRequestPath(request), ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getDefaultMessage(),
                        fe.getRejectedValue()
                ))
                .toList();

        ErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.ERROR_TYPE_VALIDATION,
                MessageConstants.VALIDATION_FAILED,
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles Jakarta Bean Validation constraint violations (e.g., @PathVariable, @RequestParam).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Constraint violation for request {}: {}", getRequestPath(request), ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> new ErrorResponse.FieldError(
                        cv.getPropertyPath().toString(),
                        cv.getMessage(),
                        cv.getInvalidValue()
                ))
                .toList();

        ErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.ERROR_TYPE_VALIDATION,
                MessageConstants.CONSTRAINT_VIOLATION,
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles database constraint violations (unique key, foreign key, etc.).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Data integrity violation for request {}: {}", getRequestPath(request), ex.getMessage());

        String message = MessageConstants.DATA_INTEGRITY_VIOLATION;
        if (ex.getMostSpecificCause() != null) {
            String cause = ex.getMostSpecificCause().getMessage();
            if (cause != null && cause.contains("unique")) {
                message = MessageConstants.UNIQUE_CONSTRAINT_VIOLATION;
            } else if (cause != null && cause.contains("foreign key")) {
                message = MessageConstants.FK_CONSTRAINT_VIOLATION;
            }
        }

        ErrorResponse response = buildErrorResponse(
                HttpStatus.CONFLICT,
                MessageConstants.ERROR_TYPE_DATA_INTEGRITY,
                message,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles resource not found exceptions.
     */
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(
            ProjectNotFoundException ex, HttpServletRequest request) {

        log.info("Resource not found: {}", ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                MessageConstants.ERROR_TYPE_NOT_FOUND,
                ex.getMessage(),
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles duplicate name conflicts.
     */
    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateName(
            DuplicateNameException ex, HttpServletRequest request) {

        log.warn("Duplicate name conflict: {}", ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.CONFLICT,
                MessageConstants.ERROR_TYPE_DUPLICATE,
                ex.getMessage(),
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles custom validation exceptions with optional field errors.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {

        log.warn("Validation exception: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = null;
        if (ex.getFieldErrors() != null && !ex.getFieldErrors().isEmpty()) {
            fieldErrors = ex.getFieldErrors().stream()
                    .map(fe -> new ErrorResponse.FieldError(
                            fe.field(),
                            fe.message(),
                            fe.rejectedValue()
                    ))
                    .toList();
        }

        ErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.ERROR_TYPE_VALIDATION,
                ex.getMessage(),
                request,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles code generation pipeline failures.
     */
    @ExceptionHandler(GenerationFailedException.class)
    public ResponseEntity<ErrorResponse> handleGenerationFailed(
            GenerationFailedException ex, HttpServletRequest request) {

        log.error("Generation failed at stage '{}': {}", ex.getOperationStage(), ex.getMessage(), ex);

        String message = String.format(MessageConstants.GENERATION_FAILED, ex.getOperationStage(), ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                MessageConstants.ERROR_TYPE_GENERATION_FAILED,
                message,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles build verification failures for generated projects.
     */
    @ExceptionHandler(BuildVerificationException.class)
    public ResponseEntity<ErrorResponse> handleBuildVerificationFailed(
            BuildVerificationException ex, HttpServletRequest request) {

        log.error("Build verification failed at stage '{}': {}", ex.getOperationStage(), ex.getMessage(), ex);

        String message = "Build verification failed at stage: " + ex.getOperationStage() + " - " + ex.getMessage();

        ErrorResponse response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                MessageConstants.ERROR_TYPE_BUILD_FAILED,
                message,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles unsupported security type configuration errors.
     */
    @ExceptionHandler(UnsupportedSecurityTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedSecurityType(
            UnsupportedSecurityTypeException ex, HttpServletRequest request) {

        log.warn("Unsupported security type '{}': {}", ex.getResourceIdentifier(), ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.ERROR_TYPE_UNSUPPORTED_SECURITY,
                ex.getMessage(),
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles malformed JSON request bodies.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed request body for {}: {}", getRequestPath(request), ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.ERROR_TYPE_MALFORMED,
                MessageConstants.MALFORMED_REQUEST,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles unsupported HTTP methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                MessageConstants.ERROR_TYPE_METHOD_NOT_ALLOWED,
                String.format(MessageConstants.METHOD_NOT_ALLOWED, ex.getMethod()),
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles resource not found (404) for static resources and unmapped paths.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {

        ErrorResponse response = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                MessageConstants.ERROR_TYPE_NOT_FOUND,
                String.format(MessageConstants.RESOURCE_PATH_NOT_FOUND, ex.getResourcePath()),
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles IllegalArgumentException (e.g., invalid enum values in type conversion).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Illegal argument for {}: {}", getRequestPath(request), ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                MessageConstants.ERROR_TYPE_INVALID_ARGUMENT,
                ex.getMessage(),
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Handles authentication failures (invalid credentials, disabled/locked accounts).
     * Returns HTTP 401 with a generic error message.
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(
            AuthenticationFailedException ex, HttpServletRequest request) {

        log.warn("Authentication failed for request {}: {}", getRequestPath(request), ex.getMessage());

        ErrorResponse response = buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_FAILED",
                ex.getMessage(),
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    /**
     * Generic fallback handler for any unhandled exceptions.
     * Returns a generic message without exposing internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error on request {}: {}", getRequestPath(request), ex.getMessage(), ex);

        ErrorResponse response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                MessageConstants.ERROR_TYPE_INTERNAL_ERROR,
                MessageConstants.UNEXPECTED_ERROR,
                request,
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(REQUEST_ID_HEADER, getRequestId())
                .body(response);
    }

    // ─── Helper Methods ──────────────────────────────────────────────────────────

    private ErrorResponse buildErrorResponse(HttpStatus status, String errorType,
                                             String message, HttpServletRequest request,
                                             List<ErrorResponse.FieldError> fieldErrors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorType,
                message,
                getRequestPath(request),
                getRequestId(),
                fieldErrors != null ? fieldErrors : Collections.emptyList()
        );
    }

    private String getRequestId() {
        String requestId = MDC.get("requestId");
        return requestId != null ? requestId : "unknown";
    }

    private String getRequestPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
