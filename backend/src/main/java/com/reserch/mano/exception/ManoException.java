package com.reserch.mano.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ManoException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ManoException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "MANO_ERROR";
    }

    public ManoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "MANO_ERROR";
    }

    public ManoException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ManoException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "MANO_ERROR";
    }

    public ManoException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
