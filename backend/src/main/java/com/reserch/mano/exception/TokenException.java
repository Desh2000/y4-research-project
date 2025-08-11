package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class TokenException extends ManoException {

  public TokenException(String message) {
    super(message, HttpStatus.UNAUTHORIZED, "TOKEN_ERROR");
  }

  public TokenException(String message, Throwable cause) {
    super(message, cause, HttpStatus.UNAUTHORIZED, "TOKEN_ERROR");
  }
}
