package com.research.mano.controller.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Request DTO for simulation operations (Component 1)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequest {

    // ==================== SINGLE SIMULATION ====================

    @NotNull(message = "Intervention ID is required")
    private Long interventionId;

    @NotNull(message = "Pre-stress score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double preStressScore;

    @NotNull(message = "Pre-depression score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double preDepressionScore;

    @NotNull(message = "Pre-anxiety score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double preAnxietyScore;

    @NotNull(message = "Post-stress score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double postStressScore;

    @NotNull(message = "Post-depression score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double postDepressionScore;

    @NotNull(message = "Post-anxiety score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double postAnxietyScore;

    @NotBlank(message = "Simulation model version is required")
    private String simulationModelVersion;

    @DecimalMin(value = "0.0", message = "Confidence score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Confidence score must be <= 1.0")
    private Double confidenceScore;

    @DecimalMin(value = "0.0", message = "Noise level must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Noise level must be <= 1.0")
    private Double noiseLevel;

    // ==================== BULK SIMULATION ====================

    /**
     * For bulk simulation requests
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkSimulationRequest {

        @NotEmpty(message = "At least one simulation is required")
        @Size(max = 1000, message = "Cannot process more than 1000 simulations at once")
        private List<SimulationRequest> simulations;
    }

    /**
     * For recommendation-based simulation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationSimulationRequest {

        @NotNull(message = "Current stress score is required")
        @DecimalMin(value = "0.0") @DecimalMax(value = "1.0")
        private Double currentStressScore;

        @NotNull(message = "Current depression score is required")
        @DecimalMin(value = "0.0") @DecimalMax(value = "1.0")
        private Double currentDepressionScore;

        @NotNull(message = "Current anxiety score is required")
        @DecimalMin(value = "0.0") @DecimalMax(value = "1.0")
        private Double currentAnxietyScore;

        @Min(value = 1, message = "Must request at least 1 recommendation")
        @Max(value = 10, message = "Cannot request more than 10 recommendations")
        private Integer maxRecommendations = 5;

        private String preferredInterventionType;
        private String maxIntensityLevel;
        private Integer maxDurationWeeks;
    }
}