package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cluster Transition Entity for Component 4
 * Tracks user movements between clusters over time
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cluster_transitions", indexes = {
        @Index(name = "idx_transition_user", columnList = "user_id"),
        @Index(name = "idx_transition_from", columnList = "from_cluster_id"),
        @Index(name = "idx_transition_to", columnList = "to_cluster_id"),
        @Index(name = "idx_transition_date", columnList = "transition_date"),
        @Index(name = "idx_transition_type", columnList = "transition_type")
})
public class ClusterTransition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_cluster_id")
    private ClusterGroup fromCluster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_cluster_id", nullable = false)
    private ClusterGroup toCluster;

    @Column(name = "transition_date", nullable = false)
    private LocalDateTime transitionDate;

    // ==================== TRANSITION CLASSIFICATION ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "transition_type", nullable = false)
    private TransitionType transitionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transition_direction")
    private TransitionDirection transitionDirection;

    @Column(name = "severity_change")
    private Integer severityChange; // Positive = worsening, Negative = improving

    // ==================== SCORES AT TRANSITION ====================

    @Column(name = "stress_score_at_transition")
    private Double stressScoreAtTransition;

    @Column(name = "depression_score_at_transition")
    private Double depressionScoreAtTransition;

    @Column(name = "anxiety_score_at_transition")
    private Double anxietyScoreAtTransition;

    @Column(name = "resilience_score_at_transition")
    private Double resilienceScoreAtTransition;

    @Column(name = "overall_risk_at_transition")
    private Double overallRiskAtTransition;

    // ==================== TRIGGER INFORMATION ====================

    @Column(name = "trigger_type")
    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    @Column(name = "trigger_prediction_id")
    private Long triggerPredictionId;

    @Column(name = "trigger_intervention_id")
    private Long triggerInterventionId;

    @Column(name = "trigger_description", columnDefinition = "TEXT")
    private String triggerDescription;

    // ==================== DURATION TRACKING ====================

    @Column(name = "days_in_previous_cluster")
    private Integer daysInPreviousCluster;

    @Column(name = "previous_cluster_entry_date")
    private LocalDateTime previousClusterEntryDate;

    // ==================== CONFIDENCE ====================

    @Column(name = "assignment_confidence")
    private Double assignmentConfidence;

    @Column(name = "distance_to_new_centroid")
    private Double distanceToNewCentroid;

    @Column(name = "distance_to_old_centroid")
    private Double distanceToOldCentroid;

    // ==================== OUTCOME TRACKING ====================

    @Column(name = "outcome_tracked")
    private Boolean outcomeTracked = false;

    @Column(name = "outcome_assessment_date")
    private LocalDateTime outcomeAssessmentDate;

    @Column(name = "transition_successful")
    private Boolean transitionSuccessful; // Did user improve/stabilize?

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    // ==================== METADATA ====================

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "is_system_generated")
    private Boolean isSystemGenerated = true;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    // ==================== ENUMS ====================

    public enum TransitionType {
        INITIAL_ASSIGNMENT("Initial cluster assignment"),
        IMPROVEMENT("Moved to lower severity cluster"),
        DETERIORATION("Moved to higher severity cluster"),
        CATEGORY_CHANGE("Changed primary category"),
        LATERAL_MOVE("Same severity, different category"),
        MODEL_REASSIGNMENT("Reassigned due to model update"),
        MANUAL_OVERRIDE("Manually reassigned by professional");

        private final String description;

        TransitionType(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    public enum TransitionDirection {
        IMPROVING,
        STABLE,
        WORSENING,
        LATERAL
    }

    public enum TriggerType {
        NEW_PREDICTION,
        INTERVENTION_COMPLETION,
        PERIODIC_REASSESSMENT,
        CRISIS_EVENT,
        MODEL_UPDATE,
        MANUAL_REVIEW,
        SELF_ASSESSMENT
    }

    // ==================== CONSTRUCTORS ====================

    public ClusterTransition(User user, ClusterGroup fromCluster, ClusterGroup toCluster) {
        this.user = user;
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
        this.transitionDate = LocalDateTime.now();
        this.isSystemGenerated = true;
        determineTransitionType();
    }

    public ClusterTransition(User user, ClusterGroup fromCluster, ClusterGroup toCluster,
                             Double stress, Double depression, Double anxiety) {
        this(user, fromCluster, toCluster);
        this.stressScoreAtTransition = stress;
        this.depressionScoreAtTransition = depression;
        this.anxietyScoreAtTransition = anxiety;
        this.overallRiskAtTransition = (stress + depression + anxiety) / 3.0;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Determine transition type based on cluster changes
     */
    public void determineTransitionType() {
        if (fromCluster == null) {
            this.transitionType = TransitionType.INITIAL_ASSIGNMENT;
            this.transitionDirection = TransitionDirection.STABLE;
            this.severityChange = 0;
            return;
        }

        int fromSeverity = fromCluster.getSeverityLevel().getLevel();
        int toSeverity = toCluster.getSeverityLevel().getLevel();
        this.severityChange = toSeverity - fromSeverity;

        boolean categoryChanged = fromCluster.getPrimaryCategory() != toCluster.getPrimaryCategory();

        if (severityChange < 0) {
            this.transitionType = TransitionType.IMPROVEMENT;
            this.transitionDirection = TransitionDirection.IMPROVING;
        } else if (severityChange > 0) {
            this.transitionType = TransitionType.DETERIORATION;
            this.transitionDirection = TransitionDirection.WORSENING;
        } else if (categoryChanged) {
            this.transitionType = TransitionType.CATEGORY_CHANGE;
            this.transitionDirection = TransitionDirection.LATERAL;
        } else {
            this.transitionType = TransitionType.LATERAL_MOVE;
            this.transitionDirection = TransitionDirection.STABLE;
        }

        // Calculate days in previous cluster
        if (previousClusterEntryDate != null) {
            this.daysInPreviousCluster = (int) java.time.Duration
                    .between(previousClusterEntryDate, transitionDate).toDays();
        }
    }

    /**
     * Check if this is a positive transition
     */
    public boolean isPositiveTransition() {
        return transitionDirection == TransitionDirection.IMPROVING;
    }

    /**
     * Check if this is a negative transition
     */
    public boolean isNegativeTransition() {
        return transitionDirection == TransitionDirection.WORSENING;
    }

    /**
     * Set trigger from prediction
     */
    public void setTriggerFromPrediction(Long predictionId) {
        this.triggerType = TriggerType.NEW_PREDICTION;
        this.triggerPredictionId = predictionId;
    }

    /**
     * Set trigger from intervention
     */
    public void setTriggerFromIntervention(Long interventionId) {
        this.triggerType = TriggerType.INTERVENTION_COMPLETION;
        this.triggerInterventionId = interventionId;
    }

    /**
     * Mark outcome as tracked
     */
    public void trackOutcome(boolean successful, String notes) {
        this.outcomeTracked = true;
        this.outcomeAssessmentDate = LocalDateTime.now();
        this.transitionSuccessful = successful;
        this.followUpNotes = notes;
    }
}
