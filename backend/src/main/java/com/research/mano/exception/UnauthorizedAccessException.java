package com.research.mano.exception;

/**
 * Exception thrown when user attempts to access a resource without proper authorization
 */
public class UnauthorizedAccessException extends ManoException {

    public UnauthorizedAccessException() {
        super("UNAUTHORIZED_ACCESS", "You are not authorized to access this resource");
    }

    public UnauthorizedAccessException(String message) {
        super("UNAUTHORIZED_ACCESS", message);
    }

    public UnauthorizedAccessException(String resource, String action) {
        super("UNAUTHORIZED_ACCESS",
                String.format("You are not authorized to %s this %s", action, resource));
    }
}