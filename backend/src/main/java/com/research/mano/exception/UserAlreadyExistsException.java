package com.research.mano.exception;

/**
 * Exception thrown when attempting to create a user that already exists
 */
public class UserAlreadyExistsException extends ManoException {

    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message);
    }

    public UserAlreadyExistsException(String field, String value) {
        super("USER_ALREADY_EXISTS",
                String.format("User with %s '%s' already exists", field, value));
    }
}