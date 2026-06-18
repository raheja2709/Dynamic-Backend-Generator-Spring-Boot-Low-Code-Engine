package com.user.driven.operations.app.common.exception;

import com.user.driven.operations.app.common.util.MessageConstants;

/**
 * Thrown when an attempt is made to create or rename a resource with a name that already exists.
 *
 * @author Jatin Raheja
 */
public class DuplicateNameException extends BaseApplicationException {

    /**
     * Constructs a DuplicateNameException.
     *
     * @param resourceType the type of resource (e.g., "Project", "Entity")
     * @param name         the duplicate name that caused the conflict
     */
    public DuplicateNameException(String resourceType, String name) {
        super(
                String.format(MessageConstants.RESOURCE_DUPLICATE, resourceType, name),
                resourceType,
                name,
                "VALIDATION"
        );
    }
}
