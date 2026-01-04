package com.research.mano.exception;

/**
 * Exception thrown when user attempts to login without verifying their account
 */
public class AccountNotVerifiedException extends ManoException {

    public AccountNotVerifiedException() {
        super("ACCOUNT_NOT_VERIFIED", "Please verify your email address before logging in");
    }

    public AccountNotVerifiedException(String message) {
        super("ACCOUNT_NOT_VERIFIED", message);
    }
}