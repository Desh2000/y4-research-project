package com.research.mano.exception;

public class InvalidScoreRangeException extends ManoException {
    public InvalidScoreRangeException() {
        super("INVALID_SCORE_RANGE", "Mental health scores must be between 0.0 and 1.0");
    }

    public InvalidScoreRangeException(String scoreType, Double value) {
        super("INVALID_SCORE_RANGE", String.format("%s score %.2f is outside valid range (0.0-1.0)", scoreType, value));
    }
}