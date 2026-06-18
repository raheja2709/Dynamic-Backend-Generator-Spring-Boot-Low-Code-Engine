package com.user.driven.operations.app.common.exception;

import com.user.driven.operations.app.common.util.MessageConstants;

/**
 * Thrown when a project or entity cannot be found by its identifier.
 *
 * @author Jatin Raheja
 */
public class ProjectNotFoundException extends BaseApplicationException {

    /**
     * Constructs a ProjectNotFoundException.
     *
     * @param resourceType the type of resource not found (e.g., "Project", "Entity")
     * @param identifier   the identifier used in the lookup
     */
    public ProjectNotFoundException(String resourceType, String identifier) {
        super(
                String.format(MessageConstants.RESOURCE_NOT_FOUND, resourceType, identifier),
                resourceType,
                identifier,
                "LOOKUP"
        );
    }
}
