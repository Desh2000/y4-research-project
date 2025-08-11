package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ManoException {

  public UserAlreadyExistsException(String message) {
    super(message, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
  }

  public UserAlreadyExistsException(String identifier, String type) {
    super("User already exists with " + type + ": " + identifier, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
  }
}
