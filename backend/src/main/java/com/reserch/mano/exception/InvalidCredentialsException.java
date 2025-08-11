package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ManoException {

  public InvalidCredentialsException() {
    super("Invalid email or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
  }

  public InvalidCredentialsException(String message) {
    super(message, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
  }
}
