package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends ManoException {

  public UnauthorizedAccessException() {
    super("Access denied. Insufficient permissions", HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
  }

//  public UnauthorizedAccessException(String message) {
//    super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
//  }
//
//  public UnauthorizedAccessException(String resource) {
//    super ("Access denied to resource": + resource, HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
//  }
}
