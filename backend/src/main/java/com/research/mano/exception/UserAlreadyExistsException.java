package com.research.mano.exception;

public class UserAlreadyExistsException extends ManoException {
    public UserAlreadyExistsException(String field, String value) {
        super("USER_ALREADY_EXISTS", String.format("User already exists with %s: %s", field, value));
    }
}
