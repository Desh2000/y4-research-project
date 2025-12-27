package com.research.mano.exception;

public class UnauthorizedAccessException extends ManoException {
    public UnauthorizedAccessException() {
        super("UNAUTHORIZED_ACCESS", "Access denied");
    }

    public UnauthorizedAccessException(String resource) {
        super("UNAUTHORIZED_ACCESS", "Access denied to resource: " + resource);
    }
}