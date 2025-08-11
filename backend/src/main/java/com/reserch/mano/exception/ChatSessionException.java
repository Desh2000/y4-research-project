package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class ChatSessionException extends ManoException {

  public ChatSessionException(String message) {
    super(message, HttpStatus.BAD_REQUEST, "CHAT_SESSION_ERROR");
  }

  public ChatSessionException(String message, HttpStatus status) {
    super(message, status, "CHAT_SESSION_ERROR");
  }
}
