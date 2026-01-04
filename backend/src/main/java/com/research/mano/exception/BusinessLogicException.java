package com.research.mano.exception;

/**
 * Exception thrown when a business rule is violated
 */
public class BusinessLogicException extends ManoException {

    public BusinessLogicException(String message) {
        super("BUSINESS_LOGIC_ERROR", message);
    }

    public BusinessLogicException(String errorCode, String message) {
        super(errorCode, message);
    }
}