package com.logitrack.b2b_tradehub.exception;

/**
 * Custom Runtime Exception for handling authorization failures (HTTP 403 Forbidden).
 * Thrown when a user is authenticated but does not have the necessary permissions (role)
 * to perform the requested operation.
 */
public class ForbiddenException extends RuntimeException {

    // Standard constructor that accepts a message detailing the reason for the access denial.
    public ForbiddenException(String message) {
        super(message);
    }

    // Optional: Constructor that accepts a message and the underlying cause.
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}