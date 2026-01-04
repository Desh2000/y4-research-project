package com.research.mano.exception;

/**
 * Exception thrown when a JWT token has expired
 */
public class JwtTokenExpiredException extends ManoException {

    public JwtTokenExpiredException() {
        super("TOKEN_EXPIRED", "Your session has expired. Please login again.");
    }

    public JwtTokenExpiredException(String message) {
        super("TOKEN_EXPIRED", message);
    }
}