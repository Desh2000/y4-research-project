package com.research.mano.exception;

public class InvalidJwtTokenException extends ManoException {
    public InvalidJwtTokenException(String message) {
        super("INVALID_JWT_TOKEN", message);
    }
}