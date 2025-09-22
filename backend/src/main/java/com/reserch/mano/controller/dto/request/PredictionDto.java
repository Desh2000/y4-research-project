package com.reserch.mano.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for representing a prediction in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDto {
    private Long id;
    private double stressScore;
    private double cognitiveRiskScore;
    private String summary;
    private LocalDateTime predictedAt;
    private Long userId;
}