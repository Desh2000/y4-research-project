package com.research.mano.exception;

public class PredictionNotFoundException extends ManoException {
    public PredictionNotFoundException(Long predictionId) {
        super("PREDICTION_NOT_FOUND", "Mental health prediction not found with ID: " + predictionId);
    }
}
