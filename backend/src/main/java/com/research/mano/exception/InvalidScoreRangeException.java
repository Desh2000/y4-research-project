package com.research.mano.exception;

/**
 * Exception thrown when a score value is outside valid range (0.0 - 1.0)
 */
public class InvalidScoreRangeException extends ManoException {

    public InvalidScoreRangeException(String message) {
        super("INVALID_SCORE_RANGE", message);
    }

    public InvalidScoreRangeException(String scoreName, Double value) {
        super("INVALID_SCORE_RANGE",
                String.format("%s score %.4f is outside valid range [0.0, 1.0]", scoreName, value));
    }

    public InvalidScoreRangeException(String scoreName, Double value, Double min, Double max) {
        super("INVALID_SCORE_RANGE",
                String.format("%s score %.4f is outside valid range [%.1f, %.1f]",
                        scoreName, value, min, max));
    }

    public InvalidScoreRangeException() {
        super("INVALID_SCORE_RANGE", "Score is outside valid range");
    }
}