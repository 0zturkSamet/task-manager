package com.taskmanager.exception;

/**
 * Exception thrown when a user attempts to perform an action they don't have permission for.
 * Results in HTTP 403 Forbidden response.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
