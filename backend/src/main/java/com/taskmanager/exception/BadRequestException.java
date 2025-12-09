package com.taskmanager.exception;

/**
 * Exception thrown when a request contains invalid data or violates business rules.
 * Results in HTTP 400 Bad Request response.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
