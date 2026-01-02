package com.research.mano.exception;

/**
 * Exception thrown when a user profile is not found
 */
public class UserProfileNotFoundException extends ManoException {

    public UserProfileNotFoundException(String message) {
        super("PROFILE_NOT_FOUND", message);
    }

    public UserProfileNotFoundException(Long userId) {
        super("PROFILE_NOT_FOUND", "Profile not found for user ID: " + userId);
    }
}