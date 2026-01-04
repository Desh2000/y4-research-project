package com.research.mano.exception;

public class InvalidSessionException extends ManoException {
    public InvalidSessionException(String sessionId) {
        super("INVALID_SESSION", "Invalid or expired session: " + sessionId);
    }
}
