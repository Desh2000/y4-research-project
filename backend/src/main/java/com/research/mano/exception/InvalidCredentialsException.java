package com.research.mano.exception;

public class InvalidCredentialsException extends ManoException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid username or password");
    }
}