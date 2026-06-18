package com.user.driven.operations.app.common.exception;

/**
 * Thrown when the code generation pipeline encounters a failure.
 *
 * @author Jatin Raheja
 */
public class GenerationFailedException extends BaseApplicationException {

    /**
     * Constructs a GenerationFailedException without a cause.
     *
     * @param message the detail message describing the generation failure
     * @param stage   the generation pipeline stage where the failure occurred
     */
    public GenerationFailedException(String message, String stage) {
        super(
                message,
                "Project",
                stage,
                stage
        );
    }

    /**
     * Constructs a GenerationFailedException with an underlying cause.
     *
     * @param message the detail message describing the generation failure
     * @param stage   the generation pipeline stage where the failure occurred
     * @param cause   the underlying cause of the failure
     */
    public GenerationFailedException(String message, String stage, Throwable cause) {
        super(
                message,
                cause,
                "Project",
                stage,
                stage
        );
    }
}
