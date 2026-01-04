package com.research.mano.exception;

/**
 * Exception thrown when a JWT token is invalid or malformed
 */
public class InvalidJwtTokenException extends ManoException {

    public InvalidJwtTokenException() {
        super("INVALID_TOKEN", "Invalid authentication token");
    }

    public InvalidJwtTokenException(String message) {
        super("INVALID_TOKEN", message);
    }

    public InvalidJwtTokenException(String message, Throwable cause) {
        super("INVALID_TOKEN", message, cause);
    }
}