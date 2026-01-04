package com.research.mano.controller.responce;

import com.research.mano.entity.InterventionOutcome;
import com.research.mano.entity.InterventionOutcome.OutcomeType;
import com.research.mano.entity.InterventionOutcome.OutcomeStatus;
import com.research.mano.entity.InterventionOutcome.EffectivenessRating;
import com.research.mano.entity.InterventionOutcome.ResponseType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for InterventionOutcome entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterventionOutcomeDTO {

    private Long id;
    private String outcomeCode;
    private Long userId;
    private String userName;

    // Intervention info
    private Long interventionId;
    private String interventionName;
    private String interventionType;

    // Outcome type and status
    private OutcomeType outcomeType;
    private OutcomeStatus status;
    private String statusDescription;

    // Pre-intervention scores
    private Double preStressScore;
    private Double preDepressionScore;
    private Double preAnxietyScore;
    private Double preResilienceScore;
    private Double preOverallRisk;

    // Post-intervention scores
    private Double postStressScore;
    private Double postDepressionScore;
    private Double postAnxietyScore;
    private Double postResilienceScore;
    private Double postOverallRisk;

    // Changes (negative = improvement for stress/depression/anxiety)
    private Double stressChange;
    private Double depressionChange;
    private Double anxietyChange;
    private Double resilienceChange;
    private Double overallImprovementScore;

    // Effectiveness
    private EffectivenessRating effectivenessRating;
    private String effectivenessDescription;
    private ResponseType responseType;
    private Boolean metExpectedOutcome;
    private Double deviationFromExpected;

    // Timing
    private LocalDateTime interventionStartDate;
    private LocalDateTime interventionEndDate;
    private LocalDateTime assessmentDate;
    private Integer actualDurationWeeks;

    // Adherence
    private Double adherencePercentage;
    private Integer sessionsCompleted;
    private Integer sessionsScheduled;
    private Boolean dropout;
    private String dropoutReason;
    private Integer dropoutWeek;

    // User feedback
    private Integer userSatisfactionScore;
    private String userFeedback;
    private Boolean wouldRecommend;
    private String sideEffects;
    private String barriersFaced;

    // Simulation metadata
    private Boolean isSimulated;
    private String simulationModelVersion;
    private Double confidenceScore;
    private Double noiseLevel;
    private Long syntheticDataRecordId;

    // Cluster tracking
    private String preClusterIdentifier;
    private String postClusterIdentifier;
    private Boolean clusterTransitionOccurred;

    // Review info
    private String reviewedBy;
    private LocalDateTime reviewDate;
    private String notes;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert from Entity to DTO
     */
    public static InterventionOutcomeDTO fromEntity(InterventionOutcome entity) {
        if (entity == null) return null;

        InterventionOutcomeDTO dto = new InterventionOutcomeDTO();

        dto.setId(entity.getId());
        dto.setOutcomeCode(entity.getOutcomeCode());

        // User info
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName());
        }

        // Intervention info
        if (entity.getIntervention() != null) {
            dto.setInterventionId(entity.getIntervention().getId());
            dto.setInterventionName(entity.getIntervention().getName());
            if (entity.getIntervention().getInterventionType() != null) {
                dto.setInterventionType(entity.getIntervention().getInterventionType().getDisplayName());
            }
        }

        // Type and status
        dto.setOutcomeType(entity.getOutcomeType());
        dto.setStatus(entity.getStatus());
        if (entity.getStatus() != null) {
            dto.setStatusDescription(entity.getStatus().name().replace("_", " "));
        }

        // Pre scores
        dto.setPreStressScore(entity.getPreStressScore());
        dto.setPreDepressionScore(entity.getPreDepressionScore());
        dto.setPreAnxietyScore(entity.getPreAnxietyScore());
        dto.setPreResilienceScore(entity.getPreResilienceScore());
        dto.setPreOverallRisk(entity.getPreOverallRisk());

        // Post scores
        dto.setPostStressScore(entity.getPostStressScore());
        dto.setPostDepressionScore(entity.getPostDepressionScore());
        dto.setPostAnxietyScore(entity.getPostAnxietyScore());
        dto.setPostResilienceScore(entity.getPostResilienceScore());
        dto.setPostOverallRisk(entity.getPostOverallRisk());

        // Changes
        dto.setStressChange(entity.getStressChange());
        dto.setDepressionChange(entity.getDepressionChange());
        dto.setAnxietyChange(entity.getAnxietyChange());
        dto.setResilienceChange(entity.getResilienceChange());
        dto.setOverallImprovementScore(entity.getOverallImprovementScore());

        // Effectiveness
        dto.setEffectivenessRating(entity.getEffectivenessRating());
        if (entity.getEffectivenessRating() != null) {
            dto.setEffectivenessDescription(entity.getEffectivenessRating().getDescription());
        }
        dto.setResponseType(entity.getResponseType());
        dto.setMetExpectedOutcome(entity.getMetExpectedOutcome());
        dto.setDeviationFromExpected(entity.getDeviationFromExpected());

        // Timing
        dto.setInterventionStartDate(entity.getInterventionStartDate());
        dto.setInterventionEndDate(entity.getInterventionEndDate());
        dto.setAssessmentDate(entity.getAssessmentDate());
        dto.setActualDurationWeeks(entity.getActualDurationWeeks());

        // Adherence
        dto.setAdherencePercentage(entity.getAdherencePercentage());
        dto.setSessionsCompleted(entity.getSessionsCompleted());
        dto.setSessionsScheduled(entity.getSessionsScheduled());
        dto.setDropout(entity.getDropout());
        dto.setDropoutReason(entity.getDropoutReason());
        dto.setDropoutWeek(entity.getDropoutWeek());

        // User feedback
        dto.setUserSatisfactionScore(entity.getUserSatisfactionScore());
        dto.setUserFeedback(entity.getUserFeedback());
        dto.setWouldRecommend(entity.getWouldRecommend());
        dto.setSideEffects(entity.getSideEffects());
        dto.setBarriersFaced(entity.getBarriersFaced());

        // Simulation
        dto.setIsSimulated(entity.getIsSimulated());
        dto.setSimulationModelVersion(entity.getSimulationModelVersion());
        dto.setConfidenceScore(entity.getConfidenceScore());
        dto.setNoiseLevel(entity.getNoiseLevel());
        dto.setSyntheticDataRecordId(entity.getSyntheticDataRecordId());

        // Cluster
        dto.setPreClusterIdentifier(entity.getPreClusterIdentifier());
        dto.setPostClusterIdentifier(entity.getPostClusterIdentifier());
        dto.setClusterTransitionOccurred(entity.getClusterTransitionOccurred());

        // Review
        dto.setReviewedBy(entity.getReviewedBy());
        dto.setReviewDate(entity.getReviewDate());
        dto.setNotes(entity.getNotes());

        // Metadata
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}