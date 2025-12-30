package com.research.mano.controller.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Request DTO for InterventionOutcome operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterventionOutcomeRequest {

    // ==================== START INTERVENTION ====================

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

    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double preResilienceScore;

    // ==================== COMPLETE INTERVENTION ====================

    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double postStressScore;

    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double postDepressionScore;

    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double postAnxietyScore;

    @DecimalMin(value = "0.0", message = "Score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Score must be <= 1.0")
    private Double postResilienceScore;

    // ==================== ADHERENCE ====================

    @Min(value = 0, message = "Sessions completed cannot be negative")
    private Integer sessionsCompleted;

    @Min(value = 1, message = "Sessions scheduled must be at least 1")
    private Integer sessionsScheduled;

    // ==================== DROPOUT ====================

    private String dropoutReason;

    @Min(value = 1, message = "Dropout week must be at least 1")
    private Integer dropoutWeek;

    // ==================== USER FEEDBACK ====================

    @Min(value = 1, message = "Satisfaction score must be between 1 and 10")
    @Max(value = 10, message = "Satisfaction score must be between 1 and 10")
    private Integer userSatisfactionScore;

    @Size(max = 2000, message = "Feedback cannot exceed 2000 characters")
    private String userFeedback;

    private Boolean wouldRecommend;

    private String sideEffects; // JSON array

    private String barriersFaced; // JSON array

    // ==================== CLUSTER INFO ====================

    private String preClusterIdentifier;
    private String postClusterIdentifier;

    // ==================== REVIEW ====================

    private String reviewedBy;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;
}