package com.research.mano.exception;

public class ValidationException extends ManoException {
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", String.format("Validation error for field '%s': %s", field, message));
    }

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}