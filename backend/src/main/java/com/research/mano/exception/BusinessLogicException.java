package com.research.mano.exception;

public class BusinessLogicException extends ManoException {
    public BusinessLogicException(String message) {
        super("BUSINESS_LOGIC_ERROR", message);
    }

    public BusinessLogicException(String operation, String reason) {
        super("BUSINESS_LOGIC_ERROR", String.format("Cannot perform %s: %s", operation, reason));
    }
}