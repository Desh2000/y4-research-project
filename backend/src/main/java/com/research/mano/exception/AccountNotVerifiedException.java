package com.research.mano.exception;

public class AccountNotVerifiedException extends ManoException {
    public AccountNotVerifiedException() {
        super("ACCOUNT_NOT_VERIFIED", "Account email not verified");
    }
}