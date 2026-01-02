package com.research.mano.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * UPDATED: User Profile Entity for Extended User Information
 * Updated to support 0.0-1.0 scoring system and proper clustering
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "location")
    private String location;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    private String notificationPreferences;

    // UPDATED: Mental Health Specific Fields (0.0-1.0 range)
    @Column(name = "current_stress_score")
    private Double currentStressScore; // 0.0-1.0 range from latest prediction

    @Column(name = "current_anxiety_score")
    private Double currentAnxietyScore; // 0.0-1.0 range from latest prediction

    @Column(name = "current_depression_score")
    private Double currentDepressionScore; // 0.0-1.0 range from latest prediction

    @Column(name = "resilience_score")
    private Double resilienceScore;

    @Column(name = "last_assessment_date")
    private LocalDateTime lastAssessmentDate;

    @Column(name = "therapy_start_date")
    private LocalDateTime therapyStartDate;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    @Column(name = "mental_health_goals", columnDefinition = "TEXT")
    private String mentalHealthGoals;

    @Column(name = "support_network_size")
    private Integer supportNetworkSize;

    @Column(name = "crisis_intervention_plan", columnDefinition = "TEXT")
    private String crisisInterventionPlan;

    // UPDATED: Component-specific fields
    @Column(name = "synthetic_data_opt_in")
    private Boolean syntheticDataOptIn = false;

    @Column(name = "prediction_alerts_enabled")
    private Boolean predictionAlertsEnabled = true;

    @Column(name = "chatbot_interaction_count")
    private Integer chatbotInteractionCount = 0;

    // UPDATED: Clustering information based on new system
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_cluster_group_id")
    private ClusterGroup currentClusterGroup;

    @Column(name = "cluster_assignment_date")
    private LocalDateTime clusterAssignmentDate;

    @Column(name = "previous_cluster_identifier")
    private String previousClusterIdentifier;

    @Column(name = "cluster_stability_score")
    private Double clusterStabilityScore; // How stable user is in current cluster

    // Risk assessment flags
    @Column(name = "high_risk_alert")
    private Boolean highRiskAlert = false;

    @Column(name = "last_high_risk_date")
    private LocalDateTime lastHighRiskDate;

    @Column(name = "intervention_required")
    private Boolean interventionRequired = false;

    // Constructors
    public UserProfile() {
        super();
    }

    public UserProfile(User user) {
        this();
        this.user = user;
    }



    // UPDATED: Utility methods
    public void updateCurrentScores(Double stress, Double anxiety, Double depression) {
        this.currentStressScore = stress;
        this.currentAnxietyScore = anxiety;
        this.currentDepressionScore = depression;
        this.lastAssessmentDate = LocalDateTime.now();

        // Check for high risk
        checkHighRiskStatus();
    }

    public void assignToCluster(ClusterGroup clusterGroup) {
        // Store previous cluster for tracking
        if (this.currentClusterGroup != null) {
            this.previousClusterIdentifier = this.currentClusterGroup.getClusterIdentifier();
        }

        this.currentClusterGroup = clusterGroup;
        this.clusterAssignmentDate = LocalDateTime.now();
    }

    private void checkHighRiskStatus() {
        boolean isHighRisk = (currentStressScore != null && currentStressScore >= 0.8) ||
                (currentAnxietyScore != null && currentAnxietyScore >= 0.8) ||
                (currentDepressionScore != null && currentDepressionScore >= 0.8);

        if (isHighRisk && !Boolean.TRUE.equals(this.highRiskAlert)) {
            this.highRiskAlert = true;
            this.lastHighRiskDate = LocalDateTime.now();
            this.interventionRequired = true;
        } else if (!isHighRisk) {
            this.highRiskAlert = false;
            this.interventionRequired = false;
        }
    }

    public Double getOverallRiskScore() {
        if (currentStressScore == null && currentAnxietyScore == null && currentDepressionScore == null) {
            return null;
        }

        double stress = currentStressScore != null ? currentStressScore : 0.0;
        double anxiety = currentAnxietyScore != null ? currentAnxietyScore : 0.0;
        double depression = currentDepressionScore != null ? currentDepressionScore : 0.0;

        return (stress + anxiety + depression) / 3.0;
    }

    public void incrementChatbotInteraction() {
        this.chatbotInteractionCount = (this.chatbotInteractionCount == null) ? 1 : this.chatbotInteractionCount + 1;
    }

    public boolean hasCompletedAssessment() {
        return lastAssessmentDate != null;
    }

    public boolean isInTherapy() {
        return therapyStartDate != null;
    }

    public String getCurrentClusterIdentifier() {
        return currentClusterGroup != null ? currentClusterGroup.getClusterIdentifier() : null;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + getId() +
                ", userId=" + (user != null ? user.getId() : null) +
                ", stressScore=" + currentStressScore +
                ", anxietyScore=" + currentAnxietyScore +
                ", depressionScore=" + currentDepressionScore +
                ", cluster=" + getCurrentClusterIdentifier() +
                ", highRisk=" + highRiskAlert +
                '}';
    }

    public void setClusterAssignment(String clusterName) {
    }

    public void setCurrentStressLevel(@NotNull(message = "Stress score is required") @DecimalMin(value = "0.0", message = "Stress score must be >= 0.0") @DecimalMax(value = "1.0", message = "Stress score must be <= 1.0") Double stressScore) {
    }

    public void setCurrentAnxietyLevel(@NotNull(message = "Anxiety score is required") @DecimalMin(value = "0.0", message = "Anxiety score must be >= 0.0") @DecimalMax(value = "1.0", message = "Anxiety score must be <= 1.0") Double anxietyScore) {
    }

    public void setCurrentDepressionLevel(@NotNull(message = "Depression score is required") @DecimalMin(value = "0.0", message = "Depression score must be >= 0.0") @DecimalMax(value = "1.0", message = "Depression score must be <= 1.0") Double depressionScore) {
    }

    public @NotNull(message = "Stress score is required") @DecimalMin(value = "0.0", message = "Stress score must be >= 0.0") @DecimalMax(value = "1.0", message = "Stress score must be <= 1.0") Double getCurrentStressLevel() {
        return 0.0;
    }

    public @NotNull(message = "Depression score is required") @DecimalMin(value = "0.0", message = "Depression score must be >= 0.0") @DecimalMax(value = "1.0", message = "Depression score must be <= 1.0") Double getCurrentDepressionLevel() {
        return 0.0;
    }

    public @NotNull(message = "Anxiety score is required") @DecimalMin(value = "0.0", message = "Anxiety score must be >= 0.0") @DecimalMax(value = "1.0", message = "Anxiety score must be <= 1.0") Double getCurrentAnxietyLevel() {
        return 0.0;
    }
}