package com.research.mano.exception;

public class AlertNotFoundException extends ManoException {
    public AlertNotFoundException(Long alertId) {
        super("ALERT_NOT_FOUND", "System alert not found with ID: " + alertId);
    }
}