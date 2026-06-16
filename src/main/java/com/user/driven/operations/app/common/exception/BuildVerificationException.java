package com.user.driven.operations.app.common.exception;

import com.user.driven.operations.app.common.util.MessageConstants;

/**
 * Thrown when a generated project fails to compile during build verification.
 *
 * @author Jatin Raheja
 */
public class BuildVerificationException extends BaseApplicationException {

    /**
     * Constructs a BuildVerificationException.
     *
     * @param projectName the name of the project that failed to build
     * @param stage       the build stage where the failure occurred (e.g., "COMPILATION", "BUILD_VERIFICATION")
     * @param cause       the underlying cause of the build failure
     */
    public BuildVerificationException(String projectName, String stage, Throwable cause) {
        super(
                String.format(MessageConstants.BUILD_VERIFICATION_FAILED, projectName, stage),
                cause,
                "Project",
                projectName,
                stage
        );
    }
}
