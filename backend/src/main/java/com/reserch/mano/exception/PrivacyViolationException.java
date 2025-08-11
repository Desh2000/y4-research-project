package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class PrivacyViolationException extends ManoException {

  public PrivacyViolationException(String message) {
    super(message, HttpStatus.FORBIDDEN, "PRIVACY_VIOLATION");
  }

  public PrivacyViolationException() {
    super("Privacy consent required for this operation", HttpStatus.FORBIDDEN, "PRIVACY_VIOLATION");
  }
}
