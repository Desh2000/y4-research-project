package com.reserch.mano.exception;

public class InvalidTokenException extends TokenException {

    public InvalidTokenException() {
        super("Invalid or expired token");
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
