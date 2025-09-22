package com.reserch.mano.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new prediction.
 * This would typically be used by an internal process that calls the ML model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePredictionRequest {
    private double stressScore;
    private double cognitiveRiskScore;
    private String summary; // A brief summary from the model
}

