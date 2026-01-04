package com.research.mano.controller.request;

import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.Intervention.IntensityLevel;
import com.research.mano.entity.Intervention.EvidenceLevel;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for creating/updating Interventions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterventionRequest {

    private String interventionCode;

    @NotBlank(message = "Intervention name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Intervention type is required")
    private InterventionType interventionType;

    @NotNull(message = "Intensity level is required")
    private IntensityLevel intensityLevel;

    @Min(value = 1, message = "Duration must be at least 1 week")
    @Max(value = 52, message = "Duration cannot exceed 52 weeks")
    private Integer durationWeeks;

    @Min(value = 1, message = "Frequency must be at least 1 per week")
    @Max(value = 14, message = "Frequency cannot exceed 14 per week")
    private Integer frequencyPerWeek;

    @Min(value = 5, message = "Session duration must be at least 5 minutes")
    @Max(value = 180, message = "Session duration cannot exceed 180 minutes")
    private Integer sessionDurationMinutes;

    // Expected effects (0.0 - 1.0 range)
    @DecimalMin(value = "0.0", message = "Expected stress reduction must be >= 0")
    @DecimalMax(value = "1.0", message = "Expected stress reduction must be <= 1")
    private Double expectedStressReduction;

    @DecimalMin(value = "0.0", message = "Expected depression reduction must be >= 0")
    @DecimalMax(value = "1.0", message = "Expected depression reduction must be <= 1")
    private Double expectedDepressionReduction;

    @DecimalMin(value = "0.0", message = "Expected anxiety reduction must be >= 0")
    @DecimalMax(value = "1.0", message = "Expected anxiety reduction must be <= 1")
    private Double expectedAnxietyReduction;

    @DecimalMin(value = "0.0", message = "Expected resilience increase must be >= 0")
    @DecimalMax(value = "1.0", message = "Expected resilience increase must be <= 1")
    private Double expectedResilienceIncrease;

    // Confidence interval
    private Double effectConfidenceLower;
    private Double effectConfidenceUpper;

    // Evidence
    private EvidenceLevel evidenceLevel;
    private String researchReferences; // JSON array

    // Contraindications and recommendations
    private String contraindications; // JSON array
    private String prerequisites; // JSON array
    private String recommendedFor; // JSON array

    // Simulation parameters
    private String simulationModelId;
    private String simulationParameters; // JSON

    // Binaural beats (Component 1 feature)
    @DecimalMin(value = "0.5", message = "Binaural frequency must be >= 0.5 Hz")
    @DecimalMax(value = "40.0", message = "Binaural frequency must be <= 40 Hz")
    private Double binauralFrequencyHz;

    @Min(value = 5, message = "Binaural session must be at least 5 minutes")
    @Max(value = 120, message = "Binaural session cannot exceed 120 minutes")
    private Integer binauralSessionMinutes;

    private String complementaryResources; // JSON array of YouTube/Spotify links

    private Boolean isActive;
    private String createdBy;
}