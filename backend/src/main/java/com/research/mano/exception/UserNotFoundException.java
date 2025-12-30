package com.research.mano.exception;

public class UserNotFoundException extends ManoException {
    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }

    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "User not found with ID: " + userId);
    }
}
