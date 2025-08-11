package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class DataProcessingException extends ManoException {

    public DataProcessingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "DATA_PROCESSING_ERROR");
    }

    public DataProcessingException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "DATA_PROCESSING_ERROR");
    }
}
