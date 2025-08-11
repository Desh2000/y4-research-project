package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class SessionNotFoundException extends ManoException {

    public SessionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND");
    }

    public SessionNotFoundException(Long sessionId) {
        super("Session not found with ID: " + sessionId, HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND");
    }
}
