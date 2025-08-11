package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ManoException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

    public ValidationException(String field, String message) {
        super("Validation failed for field '" + field + "': " + message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
}
