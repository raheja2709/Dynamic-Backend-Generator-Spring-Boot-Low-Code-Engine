package com.user.driven.operations.app.common.exception;

import com.user.driven.operations.app.common.util.MessageConstants;

/**
 * Thrown when an invalid or unsupported security type is specified in a project definition.
 *
 * @author Jatin Raheja
 */
public class UnsupportedSecurityTypeException extends BaseApplicationException {

    /**
     * Constructs an UnsupportedSecurityTypeException.
     *
     * @param securityType the unsupported security type value that was provided
     */
    public UnsupportedSecurityTypeException(String securityType) {
        super(
                String.format(MessageConstants.UNSUPPORTED_SECURITY_TYPE, securityType),
                "Project",
                securityType,
                "SECURITY_GENERATION"
        );
    }
}
