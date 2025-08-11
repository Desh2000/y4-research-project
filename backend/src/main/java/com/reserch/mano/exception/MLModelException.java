package com.reserch.mano.exception;

import org.springframework.http.HttpStatus;

public class MLModelException extends ManoException {

    public MLModelException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "ML_MODEL_ERROR");
    }

    public MLModelException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE, "ML_MODEL_ERROR");
    }

    public MLModelException(String model, String operation) {
        super("ML Model error in " + model + " during " + operation, HttpStatus.SERVICE_UNAVAILABLE, "ML_MODEL_ERROR");
    }
}
