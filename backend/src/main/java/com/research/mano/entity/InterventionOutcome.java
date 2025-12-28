package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Intervention Outcome Entity for Component 1
 * Tracks the actual results of interventions applied to users (real or simulated).
 *
 * This entity captures:
 * - Pre and post intervention mental health scores
 * - Calculated improvements
 * - Comparison with expected outcomes
 * - User feedback and adherence metrics
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "intervention_outcomes")
public class InterventionOutcome extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Null for synthetic/simulated outcomes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    @Column(name = "outcome_code", unique = true, nullable = false)
    private String outcomeCode; // Unique identifier for this outcome record

    // ==================== OUTCOME TYPE ====================
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome_type", nullable = false)
    private OutcomeType outcomeType;

    // ==================== PRE-INTERVENTION SCORES (0.0-1.0) ====================
    @Column(name = "pre_stress_score", nullable = false)
    private Double preStressScore;

    @Column(name = "pre_depression_score", nullable = false)
    private Double preDepressionScore;

    @Column(name = "pre_anxiety_score", nullable = false)
    private Double preAnxietyScore;

    @Column(name = "pre_resilience_score")
    private Double preResilienceScore;

    @Column(name = "pre_overall_risk")
    private Double preOverallRisk;

    // ==================== POST-INTERVENTION SCORES (0.0-1.0) ====================
    @Column(name = "post_stress_score")
    private Double postStressScore;

    @Column(name = "post_depression_score")
    private Double postDepressionScore;

    @Column(name = "post_anxiety_score")
    private Double postAnxietyScore;

    @Column(name = "post_resilience_score")
    private Double postResilienceScore;

    @Column(name = "post_overall_risk")
    private Double postOverallRisk;

    // ==================== CALCULATED CHANGES ====================
    @Column(name = "stress_change")
    private Double stressChange; // Negative = improvement

    @Column(name = "depression_change")
    private Double depressionChange;

    @Column(name = "anxiety_change")
    private Double anxietyChange;

    @Column(name = "resilience_change")
    private Double resilienceChange; // Positive = improvement

    @Column(name = "overall_improvement_score")
    private Double overallImprovementScore; // Weighted average of improvements

    // ==================== TIMING ====================
    @Column(name = "intervention_start_date", nullable = false)
    private LocalDateTime interventionStartDate;

    @Column(name = "intervention_end_date")
    private LocalDateTime interventionEndDate;

    @Column(name = "assessment_date")
    private LocalDateTime assessmentDate; // When post-scores were measured

    @Column(name = "actual_duration_weeks")
    private Integer actualDurationWeeks;

    // ==================== ADHERENCE METRICS ====================
    @Column(name = "adherence_percentage")
    private Double adherencePercentage; // 0-100, how well user followed the intervention

    @Column(name = "sessions_completed")
    private Integer sessionsCompleted;

    @Column(name = "sessions_scheduled")
    private Integer sessionsScheduled;

    @Column(name = "dropout")
    private Boolean dropout = false;

    @Column(name = "dropout_reason", columnDefinition = "TEXT")
    private String dropoutReason;

    @Column(name = "dropout_week")
    private Integer dropoutWeek;

    // ==================== EFFECTIVENESS ANALYSIS ====================
    @Column(name = "effectiveness_rating")
    @Enumerated(EnumType.STRING)
    private EffectivenessRating effectivenessRating;

    @Column(name = "met_expected_outcome")
    private Boolean metExpectedOutcome; // Did actual results match expected?

    @Column(name = "deviation_from_expected")
    private Double deviationFromExpected; // Percentage deviation from expected improvement

    @Column(name = "response_type")
    @Enumerated(EnumType.STRING)
    private ResponseType responseType; // How user responded to intervention

    // ==================== USER FEEDBACK ====================
    @Column(name = "user_satisfaction_score")
    private Integer userSatisfactionScore; // 1-10 scale

    @Column(name = "user_feedback", columnDefinition = "TEXT")
    private String userFeedback;

    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    @Column(name = "side_effects", columnDefinition = "TEXT")
    private String sideEffects; // JSON array of reported side effects

    @Column(name = "barriers_faced", columnDefinition = "TEXT")
    private String barriersFaced; // JSON array of barriers to completion

    // ==================== SIMULATION METADATA (Component 1) ====================
    @Column(name = "is_simulated")
    private Boolean isSimulated = false; // True if this is synthetic data

    @Column(name = "simulation_model_version")
    private String simulationModelVersion;

    @Column(name = "simulation_parameters", columnDefinition = "TEXT")
    private String simulationParameters; // JSON of parameters used in simulation

    @Column(name = "synthetic_data_record_id")
    private Long syntheticDataRecordId; // Link to SyntheticDataRecord if simulated

    @Column(name = "confidence_score")
    private Double confidenceScore; // Confidence in the outcome (for simulations)

    @Column(name = "noise_level")
    private Double noiseLevel; // Amount of noise added (for differential privacy)

    // ==================== CLUSTER TRACKING ====================
    @Column(name = "pre_cluster_identifier")
    private String preClusterIdentifier; // Cluster before intervention

    @Column(name = "post_cluster_identifier")
    private String postClusterIdentifier; // Cluster after intervention

    @Column(name = "cluster_transition_occurred")
    private Boolean clusterTransitionOccurred = false;

    // ==================== STATUS ====================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutcomeStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reviewed_by")
    private String reviewedBy; // Healthcare professional who reviewed

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    // ==================== CONSTRUCTORS ====================

    public InterventionOutcome(User user, Intervention intervention,
                               Double preStress, Double preDepression, Double preAnxiety) {
        this.user = user;
        this.intervention = intervention;
        this.preStressScore = preStress;
        this.preDepressionScore = preDepression;
        this.preAnxietyScore = preAnxiety;
        this.preOverallRisk = calculateOverallRisk(preStress, preDepression, preAnxiety);
        this.interventionStartDate = LocalDateTime.now();
        this.status = OutcomeStatus.IN_PROGRESS;
        this.outcomeType = OutcomeType.REAL;
        this.outcomeCode = generateOutcomeCode();
    }

    // Constructor for simulated outcomes
    public InterventionOutcome(Intervention intervention,
                               Double preStress, Double preDepression, Double preAnxiety,
                               String simulationModelVersion) {
        this.intervention = intervention;
        this.preStressScore = preStress;
        this.preDepressionScore = preDepression;
        this.preAnxietyScore = preAnxiety;
        this.preOverallRisk = calculateOverallRisk(preStress, preDepression, preAnxiety);
        this.interventionStartDate = LocalDateTime.now();
        this.status = OutcomeStatus.SIMULATED;
        this.outcomeType = OutcomeType.SIMULATED;
        this.isSimulated = true;
        this.simulationModelVersion = simulationModelVersion;
        this.outcomeCode = generateOutcomeCode();
    }

    // ==================== UTILITY METHODS ====================

    private String generateOutcomeCode() {
        String prefix = isSimulated != null && isSimulated ? "SIM" : "REAL";
        return prefix + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    private Double calculateOverallRisk(Double stress, Double depression, Double anxiety) {
        if (stress == null || depression == null || anxiety == null) return null;
        return (stress + depression + anxiety) / 3.0;
    }

    /**
     * Complete the intervention and calculate all outcome metrics
     */
    public void completeIntervention(Double postStress, Double postDepression, Double postAnxiety) {
        this.postStressScore = postStress;
        this.postDepressionScore = postDepression;
        this.postAnxietyScore = postAnxiety;
        this.postOverallRisk = calculateOverallRisk(postStress, postDepression, postAnxiety);
        this.assessmentDate = LocalDateTime.now();
        this.interventionEndDate = LocalDateTime.now();

        calculateChanges();
        determineEffectiveness();
        checkClusterTransition();

        this.status = OutcomeStatus.COMPLETED;
    }

    /**
     * Calculate score changes (negative = improvement for stress/depression/anxiety)
     */
    public void calculateChanges() {
        if (postStressScore != null && preStressScore != null) {
            this.stressChange = postStressScore - preStressScore;
        }
        if (postDepressionScore != null && preDepressionScore != null) {
            this.depressionChange = postDepressionScore - preDepressionScore;
        }
        if (postAnxietyScore != null && preAnxietyScore != null) {
            this.anxietyChange = postAnxietyScore - preAnxietyScore;
        }
        if (postResilienceScore != null && preResilienceScore != null) {
            this.resilienceChange = postResilienceScore - preResilienceScore;
        }

        // Calculate overall improvement (convert to positive = good)
        double stressImprove = stressChange != null ? -stressChange : 0;
        double depressionImprove = depressionChange != null ? -depressionChange : 0;
        double anxietyImprove = anxietyChange != null ? -anxietyChange : 0;
        this.overallImprovementScore = (stressImprove + depressionImprove + anxietyImprove) / 3.0;
    }

    /**
     * Determine effectiveness rating based on improvement
     */
    public void determineEffectiveness() {
        if (overallImprovementScore == null) {
            this.effectivenessRating = EffectivenessRating.UNKNOWN;
            return;
        }

        if (overallImprovementScore >= 0.3) {
            this.effectivenessRating = EffectivenessRating.HIGHLY_EFFECTIVE;
            this.responseType = ResponseType.EXCELLENT_RESPONDER;
        } else if (overallImprovementScore >= 0.15) {
            this.effectivenessRating = EffectivenessRating.EFFECTIVE;
            this.responseType = ResponseType.GOOD_RESPONDER;
        } else if (overallImprovementScore >= 0.05) {
            this.effectivenessRating = EffectivenessRating.MODERATELY_EFFECTIVE;
            this.responseType = ResponseType.PARTIAL_RESPONDER;
        } else if (overallImprovementScore >= -0.05) {
            this.effectivenessRating = EffectivenessRating.MINIMALLY_EFFECTIVE;
            this.responseType = ResponseType.NON_RESPONDER;
        } else {
            this.effectivenessRating = EffectivenessRating.NOT_EFFECTIVE;
            this.responseType = ResponseType.NEGATIVE_RESPONDER;
        }

        // Check against expected outcome
        if (intervention != null) {
            Double expectedImprovement = intervention.calculateTotalExpectedImprovement();
            if (expectedImprovement != null && expectedImprovement > 0) {
                this.deviationFromExpected = ((overallImprovementScore - expectedImprovement) / expectedImprovement) * 100;
                this.metExpectedOutcome = overallImprovementScore >= (expectedImprovement * 0.8); // Within 80% of expected
            }
        }
    }

    /**
     * Check if cluster transition occurred
     */
    private void checkClusterTransition() {
        if (preClusterIdentifier != null && postClusterIdentifier != null) {
            this.clusterTransitionOccurred = !preClusterIdentifier.equals(postClusterIdentifier);
        }
    }

    /**
     * Calculate adherence percentage
     */
    public void updateAdherence() {
        if (sessionsScheduled != null && sessionsScheduled > 0 && sessionsCompleted != null) {
            this.adherencePercentage = (sessionsCompleted.doubleValue() / sessionsScheduled.doubleValue()) * 100;
        }
    }

    /**
     * Mark as dropout
     */
    public void markAsDropout(String reason, int week) {
        this.dropout = true;
        this.dropoutReason = reason;
        this.dropoutWeek = week;
        this.status = OutcomeStatus.DROPPED_OUT;
    }

    /**
     * Check if outcome shows significant improvement
     */
    public boolean hasSignificantImprovement() {
        return overallImprovementScore != null && overallImprovementScore >= 0.1;
    }

    /**
     * Get a summary string
     */
    public String getSummary() {
        return String.format("Intervention: %s | Improvement: %.2f | Effectiveness: %s",
                intervention != null ? intervention.getName() : "Unknown",
                overallImprovementScore != null ? overallImprovementScore : 0.0,
                effectivenessRating != null ? effectivenessRating.name() : "Unknown");
    }

    // ==================== ENUMS ====================

    public enum OutcomeType {
        REAL,           // Actual user outcome
        SIMULATED,      // Generated by simulation
        SYNTHETIC,      // From synthetic data generation
        PROJECTED       // Projected/predicted outcome
    }

    public enum OutcomeStatus {
        IN_PROGRESS,    // Intervention ongoing
        COMPLETED,      // Intervention completed with post-assessment
        DROPPED_OUT,    // User dropped out
        SIMULATED,      // Simulated outcome
        PENDING_REVIEW, // Awaiting professional review
        ARCHIVED        // Old record, archived
    }

    public enum EffectivenessRating {
        HIGHLY_EFFECTIVE("30%+ improvement"),
        EFFECTIVE("15-30% improvement"),
        MODERATELY_EFFECTIVE("5-15% improvement"),
        MINIMALLY_EFFECTIVE("0-5% improvement"),
        NOT_EFFECTIVE("No improvement or worsening"),
        UNKNOWN("Cannot determine");

        private final String description;

        EffectivenessRating(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ResponseType {
        EXCELLENT_RESPONDER,
        GOOD_RESPONDER,
        PARTIAL_RESPONDER,
        NON_RESPONDER,
        NEGATIVE_RESPONDER
    }
}