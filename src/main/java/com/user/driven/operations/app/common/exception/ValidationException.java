package com.user.driven.operations.app.common.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thrown when custom validation logic detects invalid input.
 * Can optionally carry a list of field-level errors.
 *
 * @author Jatin Raheja
 */
public class ValidationException extends BaseApplicationException {

    private final List<FieldError> fieldErrors;

    /**
     * Constructs a ValidationException with a message.
     *
     * @param message the validation failure message
     */
    public ValidationException(String message) {
        super(message, "Request", "N/A", "VALIDATION");
        this.fieldErrors = new ArrayList<>();
    }

    /**
     * Constructs a ValidationException with a message and field errors.
     *
     * @param message     the validation failure message
     * @param fieldErrors the list of field-level errors
     */
    public ValidationException(String message, List<FieldError> fieldErrors) {
        super(message, "Request", "N/A", "VALIDATION");
        this.fieldErrors = fieldErrors != null ? new ArrayList<>(fieldErrors) : new ArrayList<>();
    }

    public List<FieldError> getFieldErrors() {
        return Collections.unmodifiableList(fieldErrors);
    }

    /**
     * Represents a single field-level validation error.
     */
    public record FieldError(String field, String message, Object rejectedValue) {
    }
}
