package com.research.mano.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Base Exception for Mental Health Application
 * All custom exceptions should extend this class
 */
public class ManoException extends RuntimeException {

    private final String errorCode;
    private final Object[] args;

    public ManoException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
        this.args = new Object[0];
    }

    public ManoException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
        this.args = new Object[0];
    }

    public ManoException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public ManoException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public ManoException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public ManoException(String errorCode, Object[] args) {

        this.errorCode = errorCode;
        this.args = args;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }
}
