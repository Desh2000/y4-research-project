package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class ResourceLimitExceededException extends ManoException {

  public ResourceLimitExceededException(String resource, int limit) {
    super("Resource limit exceeded for " + resource + ". Maximum allowed: " + limit, HttpStatus.TOO_MANY_REQUESTS, "RESOURCE_LIMIT_EXCEEDED");
  }

  public ResourceLimitExceededException(String message) {
    super(message, HttpStatus.TOO_MANY_REQUESTS, "RESOURCE_LIMIT_EXCEEDED");
  }
}