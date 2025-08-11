package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends ManoException {

  public ServiceUnavailableException(String service) {
    super(service + " service is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE");
  }

  public ServiceUnavailableException(String message, Throwable cause) {
    super(message, cause, HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE");
  }
}
