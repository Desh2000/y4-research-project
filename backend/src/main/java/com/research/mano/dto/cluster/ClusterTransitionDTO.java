package com.research.mano.dto.cluster;

import com.research.mano.entity.ClusterTransition;
import com.research.mano.entity.ClusterTransition.TransitionType;
import com.research.mano.entity.ClusterTransition.TransitionDirection;
import com.research.mano.entity.ClusterTransition.TriggerType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for Cluster Transition
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterTransitionDTO {

    private Long id;
    private Long userId;
    private String userName;

    // From Cluster
    private Long fromClusterId;
    private String fromClusterIdentifier;
    private String fromClusterName;

    // To Cluster
    private Long toClusterId;
    private String toClusterIdentifier;
    private String toClusterName;

    // Transition Info
    private LocalDateTime transitionDate;
    private TransitionType transitionType;
    private String transitionTypeDescription;
    private TransitionDirection transitionDirection;
    private Integer severityChange;

    // Scores at Transition
    private Double stressScoreAtTransition;
    private Double depressionScoreAtTransition;
    private Double anxietyScoreAtTransition;
    private Double resilienceScoreAtTransition;
    private Double overallRiskAtTransition;

    // Trigger
    private TriggerType triggerType;
    private Long triggerPredictionId;
    private Long triggerInterventionId;
    private String triggerDescription;

    // Duration
    private Integer daysInPreviousCluster;

    // Confidence
    private Double assignmentConfidence;
    private Double distanceToNewCentroid;

    // Outcome
    private Boolean outcomeTracked;
    private Boolean transitionSuccessful;
    private String followUpNotes;

    // Metadata
    private String modelVersion;
    private Boolean isSystemGenerated;

    // Flags
    private Boolean isPositiveTransition;
    private Boolean isNegativeTransition;

    /**
     * Convert from Entity
     */
    public static ClusterTransitionDTO fromEntity(ClusterTransition entity) {
        if (entity == null) return null;

        ClusterTransitionDTOBuilder builder = ClusterTransitionDTO.builder()
                .id(entity.getId())
                .transitionDate(entity.getTransitionDate())
                .transitionType(entity.getTransitionType())
                .transitionTypeDescription(entity.getTransitionType() != null
                        ? entity.getTransitionType().getDescription() : null)
                .transitionDirection(entity.getTransitionDirection())
                .severityChange(entity.getSeverityChange())
                .stressScoreAtTransition(entity.getStressScoreAtTransition())
                .depressionScoreAtTransition(entity.getDepressionScoreAtTransition())
                .anxietyScoreAtTransition(entity.getAnxietyScoreAtTransition())
                .resilienceScoreAtTransition(entity.getResilienceScoreAtTransition())
                .overallRiskAtTransition(entity.getOverallRiskAtTransition())
                .triggerType(entity.getTriggerType())
                .triggerPredictionId(entity.getTriggerPredictionId())
                .triggerInterventionId(entity.getTriggerInterventionId())
                .triggerDescription(entity.getTriggerDescription())
                .daysInPreviousCluster(entity.getDaysInPreviousCluster())
                .assignmentConfidence(entity.getAssignmentConfidence())
                .distanceToNewCentroid(entity.getDistanceToNewCentroid())
                .outcomeTracked(entity.getOutcomeTracked())
                .transitionSuccessful(entity.getTransitionSuccessful())
                .followUpNotes(entity.getFollowUpNotes())
                .modelVersion(entity.getModelVersion())
                .isSystemGenerated(entity.getIsSystemGenerated())
                .isPositiveTransition(entity.isPositiveTransition())
                .isNegativeTransition(entity.isNegativeTransition());

        // User info
        if (entity.getUser() != null) {
            builder.userId(entity.getUser().getId());
            builder.userName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName());
        }

        // From cluster info
        if (entity.getFromCluster() != null) {
            builder.fromClusterId(entity.getFromCluster().getId());
            builder.fromClusterIdentifier(entity.getFromCluster().getClusterIdentifier());
            builder.fromClusterName(entity.getFromCluster().getClusterName());
        }

        // To cluster info
        if (entity.getToCluster() != null) {
            builder.toClusterId(entity.getToCluster().getId());
            builder.toClusterIdentifier(entity.getToCluster().getClusterIdentifier());
            builder.toClusterName(entity.getToCluster().getClusterName());
        }

        return builder.build();
    }
}