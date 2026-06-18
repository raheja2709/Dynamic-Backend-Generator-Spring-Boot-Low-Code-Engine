package com.user.driven.operations.app.common.util;

/**
 * Centralized message constants used across the application.
 * Prevents hardcoded strings and ensures consistency.
 */
public final class MessageConstants {

    private MessageConstants() {} // Prevent instantiation

    // === Resource Not Found ===
    public static final String PROJECT_NOT_FOUND = "Project not found with id: %s";
    public static final String ENTITY_NOT_FOUND = "Entity not found with id: %s";
    public static final String RESOURCE_NOT_FOUND = "%s not found with identifier: %s";

    // === Duplicate Name ===
    public static final String PROJECT_DUPLICATE_NAME = "Project with name '%s' already exists";
    public static final String ENTITY_DUPLICATE_NAME = "Entity with name '%s' already exists in this project";
    public static final String RESOURCE_DUPLICATE = "%s with name '%s' already exists";

    // === Entity/Field Limits ===
    public static final int MAX_ENTITIES_PER_PROJECT = 50;
    public static final int MAX_FIELDS_PER_ENTITY = 100;
    public static final String MAX_ENTITIES_EXCEEDED = "Maximum of %d entities per project exceeded. Current count: %d";
    public static final String MAX_FIELDS_EXCEEDED = "Maximum of %d fields per entity exceeded. Provided: %d";

    // === Validation ===
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String CONSTRAINT_VIOLATION = "Constraint violation";
    public static final String MALFORMED_REQUEST = "Request body is malformed or contains invalid values";
    public static final String METHOD_NOT_ALLOWED = "HTTP method '%s' is not supported for this endpoint";
    public static final String RESOURCE_PATH_NOT_FOUND = "Resource not found: %s";
    public static final String PAGE_MUST_BE_NON_NEGATIVE = "Page index must be >= 0";
    public static final String SIZE_OUT_OF_RANGE = "Page size must be between 1 and 100";

    // === Security ===
    public static final String UNSUPPORTED_SECURITY_TYPE = "Unsupported security type: '%s'. Valid types are: JWT, OAUTH2, SESSION_BASED, BASIC_AUTH";
    public static final String VALID_SECURITY_TYPES = "JWT, OAUTH2, SESSION_BASED, BASIC_AUTH";

    // === Password Validation ===
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 8 characters";
    public static final String PASSWORD_TOO_LONG = "Password must not exceed 128 characters";
    public static final String PASSWORD_MISSING_UPPERCASE = "Password must contain at least one uppercase letter";
    public static final String PASSWORD_MISSING_LOWERCASE = "Password must contain at least one lowercase letter";
    public static final String PASSWORD_MISSING_DIGIT = "Password must contain at least one digit";
    public static final String PASSWORD_MISSING_SPECIAL = "Password must contain at least one special character";

    // === Authentication ===
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCOUNT_DISABLED = "Account is disabled";
    public static final String ACCOUNT_LOCKED = "Account is locked";

    // === Generation ===
    public static final String GENERATION_FAILED = "Generation failed at stage: %s - %s";
    public static final String BUILD_VERIFICATION_FAILED = "Build verification failed for project '%s' at stage: %s";
    public static final String BUILD_COMPILATION_FAILED = "Generated project compilation failed";
    public static final String GENERATION_PIPELINE_ERROR = "Generation pipeline error";
    public static final String NO_SECURITY_GENERATOR = "No security generator available for type: %s. Supported types: JWT. Set securityEnabled=false or use a supported type.";

    // === Data Integrity ===
    public static final String DATA_INTEGRITY_VIOLATION = "A data integrity constraint was violated";
    public static final String UNIQUE_CONSTRAINT_VIOLATION = "A resource with the given unique value already exists";
    public static final String FK_CONSTRAINT_VIOLATION = "Referenced resource does not exist";

    // === Error Types ===
    public static final String ERROR_TYPE_VALIDATION = "VALIDATION_ERROR";
    public static final String ERROR_TYPE_NOT_FOUND = "NOT_FOUND";
    public static final String ERROR_TYPE_DUPLICATE = "DUPLICATE_RESOURCE";
    public static final String ERROR_TYPE_DATA_INTEGRITY = "DATA_INTEGRITY_VIOLATION";
    public static final String ERROR_TYPE_GENERATION_FAILED = "GENERATION_FAILED";
    public static final String ERROR_TYPE_BUILD_FAILED = "BUILD_VERIFICATION_FAILED";
    public static final String ERROR_TYPE_UNSUPPORTED_SECURITY = "UNSUPPORTED_SECURITY_TYPE";
    public static final String ERROR_TYPE_MALFORMED = "MALFORMED_REQUEST";
    public static final String ERROR_TYPE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    public static final String ERROR_TYPE_INVALID_ARGUMENT = "INVALID_ARGUMENT";
    public static final String ERROR_TYPE_INTERNAL_ERROR = "INTERNAL_SERVER_ERROR";

    // === Generic ===
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    public static final String UNSUPPORTED_FIELD_TYPE = "Unsupported field type: '%s'. Valid types: %s";

    // === JWT Authentication ===
    public static final String JWT_AUTHENTICATION_FAILED = "JWT authentication failed: {}";
    public static final String JWT_TOKEN_EXPIRED = "JWT token has expired";
    public static final String JWT_TOKEN_MALFORMED = "JWT token is malformed";
    public static final String JWT_TOKEN_INVALID = "JWT token is invalid";

    // === Rate Limiting ===
    public static final String RATE_LIMIT_EXCEEDED = "Rate limit exceeded. Maximum %d requests per %d seconds.";
    public static final String ERROR_TYPE_RATE_LIMITED = "RATE_LIMITED";

    // === Logging ===
    public static final String LOG_CLEANUP_START = "Starting log cleanup. Deleting records older than {} (retention: {} days)";
    public static final String LOG_CLEANUP_SUCCESS = "Log cleanup completed successfully";
    public static final String LOG_CLEANUP_FAILED = "Log cleanup failed: {}";
    public static final String AUDIT_PERSIST_FAILED = "Failed to persist audit log entry: {}";
    public static final String ENV_VALIDATION_PASSED = "Environment validation passed for profile: {}";
    public static final String ENV_VALIDATION_FAILED = "Required environment variable(s) not set for '%s' profile: %s. Please set these variables or refer to .env.example for documentation.";
}
