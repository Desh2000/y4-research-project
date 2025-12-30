package com.research.mano.exception;

public class SyntheticDataRecordNotFoundException extends ManoException {
    public SyntheticDataRecordNotFoundException(String recordId) {
        super("SYNTHETIC_DATA_NOT_FOUND", "Synthetic data record not found: " + recordId);
    }

    public SyntheticDataRecordNotFoundException(Long id) {
        super("SYNTHETIC_DATA_NOT_FOUND", "Synthetic data record not found with ID: " + id);
    }
}
