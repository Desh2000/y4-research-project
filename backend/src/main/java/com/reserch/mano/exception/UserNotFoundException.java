package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ManoException {

    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    public UserNotFoundException(String identifier, String type) {
        super("User not found with " + type + ": " + identifier, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }
}
