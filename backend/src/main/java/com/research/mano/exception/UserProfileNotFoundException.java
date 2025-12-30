package com.research.mano.exception;

public class UserProfileNotFoundException extends ManoException {
    public UserProfileNotFoundException(Long userId) {
        super("USER_PROFILE_NOT_FOUND", "User profile not found for user ID: " + userId);
    }
}