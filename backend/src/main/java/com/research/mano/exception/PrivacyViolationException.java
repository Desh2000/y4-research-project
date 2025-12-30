package com.research.mano.exception;

public class PrivacyViolationException extends ManoException {
    public PrivacyViolationException(String message) {
        super("PRIVACY_VIOLATION", message);
    }
}