package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class ConsentRequiredException extends ManoException {

    public ConsentRequiredException(String consentType) {
        super("User consent required for: " + consentType, HttpStatus.FORBIDDEN, "CONSENT_REQUIRED");
    }

    public ConsentRequiredException() {
        super("User consent required for this operation", HttpStatus.FORBIDDEN, "CONSENT_REQUIRED");
    }
}