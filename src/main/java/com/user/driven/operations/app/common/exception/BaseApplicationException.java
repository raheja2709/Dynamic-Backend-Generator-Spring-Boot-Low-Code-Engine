package com.user.driven.operations.app.common.exception;

/**
 * Abstract base exception class for all custom application exceptions.
 * Carries contextual metadata: resource type, resource identifier, and operation stage.
 *
 * @author Jatin Raheja
 */
public abstract class BaseApplicationException extends RuntimeException {

    private final String resourceType;
    private final String resourceIdentifier;
    private final String operationStage;

    /**
     * Constructs a new BaseApplicationException with message and context fields.
     *
     * @param message            the detail message
     * @param resourceType       the type of resource involved (e.g., "Project", "Entity")
     * @param resourceIdentifier the identifier or input that caused the failure
     * @param operationStage     the operation stage where the failure occurred
     */
    protected BaseApplicationException(String message, String resourceType, String resourceIdentifier, String operationStage) {
        super(message);
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
        this.operationStage = operationStage;
    }

    /**
     * Constructs a new BaseApplicationException with message, cause, and context fields.
     *
     * @param message            the detail message
     * @param cause              the underlying cause
     * @param resourceType       the type of resource involved
     * @param resourceIdentifier the identifier or input that caused the failure
     * @param operationStage     the operation stage where the failure occurred
     */
    protected BaseApplicationException(String message, Throwable cause, String resourceType, String resourceIdentifier, String operationStage) {
        super(message, cause);
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
        this.operationStage = operationStage;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public String getOperationStage() {
        return operationStage;
    }
}
