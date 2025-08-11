package com.reserch.mano.exception;

public class ExpiredTokenException extends TokenException {

    public ExpiredTokenException() {
        super("Token has expired");
    }

    public ExpiredTokenException(String message) {
        super(message);
    }
}
