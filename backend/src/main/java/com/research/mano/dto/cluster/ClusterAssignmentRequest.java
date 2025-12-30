package com.research.mano.dto.cluster;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for cluster assignment operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterAssignmentRequest {

    @NotNull(message = "Stress score is required")
    @DecimalMin(value = "0.0", message = "Stress score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Stress score must be <= 1.0")
    private Double stressScore;

    @NotNull(message = "Depression score is required")
    @DecimalMin(value = "0.0", message = "Depression score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Depression score must be <= 1.0")
    private Double depressionScore;

    @NotNull(message = "Anxiety score is required")
    @DecimalMin(value = "0.0", message = "Anxiety score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Anxiety score must be <= 1.0")
    private Double anxietyScore;

    @DecimalMin(value = "0.0", message = "Resilience score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Resilience score must be <= 1.0")
    private Double resilienceScore;

    private Long predictionId; // Optional: ID of prediction that triggered this
    private Long interventionId; // Optional: ID of intervention that triggered this
    private String triggerDescription; // Optional: Description of what triggered reassignment
}