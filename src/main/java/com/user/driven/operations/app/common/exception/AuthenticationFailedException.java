package com.user.driven.operations.app.common.exception;

/**
 * Thrown when user authentication fails due to invalid credentials,
 * disabled account, or locked account.
 * Maps to HTTP 401 Unauthorized.
 *
 * @author Jatin Raheja
 */
public class AuthenticationFailedException extends RuntimeException {

    /**
     * Constructs an AuthenticationFailedException with a message.
     *
     * @param message the detail message describing why authentication failed
     */
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
