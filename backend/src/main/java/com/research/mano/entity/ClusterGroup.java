package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cluster Group Entity for Component 4
 * Represents a GMM-based cluster for community-driven resilience support
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cluster_groups", indexes = {
        @Index(name = "idx_cluster_identifier", columnList = "cluster_identifier"),
        @Index(name = "idx_cluster_category", columnList = "primary_category"),
        @Index(name = "idx_cluster_active", columnList = "is_active")
})
public class ClusterGroup extends BaseEntity {

    @Column(name = "cluster_identifier", unique = true, nullable = false)
    private String clusterIdentifier; // e.g., "STRESS_HIGH", "ANXIETY_MEDIUM"

    @Column(name = "cluster_name", nullable = false)
    private String clusterName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cluster_description", columnDefinition = "TEXT")
    private String clusterDescription;

    // ==================== CLUSTER CLASSIFICATION ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_category", nullable = false)
    private ClusterCategory primaryCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false)
    private SeverityLevel severityLevel;

    @Column(name = "cluster_index")
    private Integer clusterIndex; // GMM cluster number (0, 1, 2, etc.)

    // ==================== GMM PARAMETERS ====================

    @Column(name = "centroid_stress")
    private Double centroidStress;

    @Column(name = "centroid_depression")
    private Double centroidDepression;

    @Column(name = "centroid_anxiety")
    private Double centroidAnxiety;

    @Column(name = "centroid_resilience")
    private Double centroidResilience;

    @Column(name = "covariance_matrix", columnDefinition = "TEXT")
    private String covarianceMatrix; // JSON representation

    @Column(name = "cluster_weight")
    private Double clusterWeight; // GMM mixture weight (pi_k)

    @Column(name = "cluster_radius")
    private Double clusterRadius; // Average distance from centroid

    // ==================== MEMBER STATISTICS ====================

    @Column(name = "member_count")
    private Long memberCount = 0L;

    @Column(name = "active_member_count")
    private Long activeMemberCount = 0L;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "avg_member_stress")
    private Double avgMemberStress;

    @Column(name = "avg_member_depression")
    private Double avgMemberDepression;

    @Column(name = "avg_member_anxiety")
    private Double avgMemberAnxiety;

    @Column(name = "avg_member_resilience")
    private Double avgMemberResilience;

    @Column(name = "average_resilience_score")
    private Double averageResilienceScore;

    @Column(name = "std_dev_stress")
    private Double stdDevStress;

    @Column(name = "std_dev_depression")
    private Double stdDevDepression;

    @Column(name = "std_dev_anxiety")
    private Double stdDevAnxiety;

    // ==================== OUTCOME TRACKING ====================

    @Column(name = "avg_improvement_rate")
    private Double avgImprovementRate; // Average improvement of members

    @Column(name = "successful_transitions")
    private Integer successfulTransitions = 0; // Members who improved to better cluster

    @Column(name = "avg_time_in_cluster_days")
    private Double avgTimeInClusterDays;

    @Column(name = "retention_rate")
    private Double retentionRate; // % of members still active

    // ==================== INTERVENTION RECOMMENDATIONS ====================

    @Column(name = "recommended_interventions", columnDefinition = "TEXT")
    private String recommendedInterventions; // JSON array of intervention IDs

    @Column(name = "primary_intervention_type")
    private String primaryInterventionType;

    @Column(name = "avg_intervention_effectiveness")
    private Double avgInterventionEffectiveness;

    @Column(name = "support_resources", columnDefinition = "TEXT")
    private String supportResources; // JSON array of resources

    // ==================== COMMUNITY FEATURES ====================

    @Column(name = "has_peer_support")
    private Boolean hasPeerSupport = false;

    @Column(name = "peer_support_group_id")
    private Long peerSupportGroupId;

    @Column(name = "community_engagement_score")
    private Double communityEngagementScore;

    @Column(name = "shared_experiences", columnDefinition = "TEXT")
    private String sharedExperiences; // JSON array of common themes

    @Column(name = "coping_strategies", columnDefinition = "TEXT")
    private String copingStrategies; // JSON array of effective strategies

    @Column(name = "peer_support_activities", columnDefinition = "TEXT")
    private String peerSupportActivities;

    @Enumerated(EnumType.STRING)
    @Column(name = "professional_support_level")
    private ProfessionalSupportLevel professionalSupportLevel;

    // ==================== MODEL METADATA ====================

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "last_model_update")
    private LocalDateTime lastModelUpdate;

    @Column(name = "silhouette_score")
    private Double silhouetteScore; // Cluster quality metric

    @Column(name = "cohesion_score")
    private Double cohesionScore;

    @Column(name = "separation_score")
    private Double separationScore;

    // ==================== STATUS ====================

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "requires_review")
    private Boolean requiresReview = false;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // ==================== RELATIONSHIPS ====================

    @OneToMany(mappedBy = "currentClusterGroup", fetch = FetchType.LAZY)
    private List<UserProfile> members = new ArrayList<>();

    // ==================== ENUMS ====================

    public enum ClusterCategory {
        STRESS("Stress-Dominant", "Primary concern is stress-related symptoms"),
        DEPRESSION("Depression-Dominant", "Primary concern is depressive symptoms"),
        ANXIETY("Anxiety-Dominant", "Primary concern is anxiety-related symptoms"),
        MIXED("Mixed Presentation", "Multiple concerns at similar levels"),
        RESILIENT("High Resilience", "Good coping mechanisms, lower risk"),
        CRISIS("Crisis Level", "Requires immediate attention");

        private final String displayName;
        private final String description;

        ClusterCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public enum SeverityLevel {
        MINIMAL(1, "Minimal", 0.0, 0.2),
        LOW(2, "Low", 0.2, 0.4),
        MODERATE(3, "Moderate", 0.4, 0.6),
        HIGH(4, "High", 0.6, 0.8),
        SEVERE(5, "Severe", 0.8, 1.0);

        private final int level;
        private final String displayName;
        private final double minScore;
        private final double maxScore;

        SeverityLevel(int level, String displayName, double minScore, double maxScore) {
            this.level = level;
            this.displayName = displayName;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        public int getLevel() { return level; }
        public String getDisplayName() { return displayName; }
        public double getMinScore() { return minScore; }
        public double getMaxScore() { return maxScore; }

        public static SeverityLevel fromScore(double score) {
            if (score < 0.2) return MINIMAL;
            if (score < 0.4) return LOW;
            if (score < 0.6) return MODERATE;
            if (score < 0.8) return HIGH;
            return SEVERE;
        }
    }

    public enum ProfessionalSupportLevel {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }

    // ==================== CONSTRUCTORS ====================

    public ClusterGroup(String clusterIdentifier, ClusterCategory category, SeverityLevel severity) {
        this.clusterIdentifier = clusterIdentifier;
        this.primaryCategory = category;
        this.severityLevel = severity;
        this.clusterName = category.getDisplayName() + " - " + severity.getDisplayName();
        this.isActive = true;
    }

    public ClusterGroup(ClusterCategory category, SeverityLevel severity,
                        Double centroidStress, Double centroidDepression,
                        Double centroidAnxiety, Double centroidResilience) {
        this(category.name() + "_" + severity.name(), category, severity);
        this.centroidStress = centroidStress;
        this.centroidDepression = centroidDepression;
        this.centroidAnxiety = centroidAnxiety;
        this.centroidResilience = centroidResilience;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Calculate Euclidean distance from a point to this cluster's centroid
     */
    public double calculateDistanceFromCentroid(double stress, double depression, double anxiety) {
        if (centroidStress == null || centroidDepression == null || centroidAnxiety == null) {
            return Double.MAX_VALUE;
        }

        double dStress = stress - centroidStress;
        double dDepression = depression - centroidDepression;
        double dAnxiety = anxiety - centroidAnxiety;

        return Math.sqrt(dStress * dStress + dDepression * dDepression + dAnxiety * dAnxiety);
    }

    /**
     * Calculate Mahalanobis distance (if covariance available)
     */
    public double calculateMahalanobisDistance(double stress, double depression, double anxiety) {
        // Simplified - would need full covariance matrix implementation
        return calculateDistanceFromCentroid(stress, depression, anxiety);
    }

    /**
     * Check if a point belongs to this cluster based on distance threshold
     */
    public boolean containsPoint(double stress, double depression, double anxiety, double threshold) {
        return calculateDistanceFromCentroid(stress, depression, anxiety) <= threshold;
    }

    /**
     * Update member statistics
     */
    public void updateMemberStatistics(List<double[]> memberScores) {
        if (memberScores == null || memberScores.isEmpty()) {
            return;
        }

        long count = memberScores.size();
        this.memberCount = count;

        // Calculate averages
        double sumStress = 0, sumDepression = 0, sumAnxiety = 0;
        for (double[] scores : memberScores) {
            sumStress += scores[0];
            sumDepression += scores[1];
            sumAnxiety += scores[2];
        }

        this.avgMemberStress = sumStress / count;
        this.avgMemberDepression = sumDepression / count;
        this.avgMemberAnxiety = sumAnxiety / count;

        // Calculate standard deviations
        double sumSqStress = 0, sumSqDepression = 0, sumSqAnxiety = 0;
        for (double[] scores : memberScores) {
            sumSqStress += Math.pow(scores[0] - avgMemberStress, 2);
            sumSqDepression += Math.pow(scores[1] - avgMemberDepression, 2);
            sumSqAnxiety += Math.pow(scores[2] - avgMemberAnxiety, 2);
        }

        this.stdDevStress = Math.sqrt(sumSqStress / count);
        this.stdDevDepression = Math.sqrt(sumSqDepression / count);
        this.stdDevAnxiety = Math.sqrt(sumSqAnxiety / count);
    }

    /**
     * Increment member count
     */
    public void addMember() {
        this.memberCount = (this.memberCount != null ? this.memberCount : 0) + 1;
        this.activeMemberCount = (this.activeMemberCount != null ? this.activeMemberCount : 0) + 1;
    }

    /**
     * Decrement member count
     */
    public void removeMember() {
        if (this.activeMemberCount != null && this.activeMemberCount > 0) {
            this.activeMemberCount--;
        }
    }

    /**
     * Record successful transition
     */
    public void recordSuccessfulTransition() {
        this.successfulTransitions = (this.successfulTransitions != null ? this.successfulTransitions : 0) + 1;
    }

    /**
     * Check if cluster is at capacity
     */
    public boolean isAtCapacity() {
        return maxCapacity != null && memberCount != null && memberCount >= maxCapacity;
    }

    /**
     * Check if cluster needs attention (high severity with many members)
     */
    public boolean needsAttention() {
        return (severityLevel == SeverityLevel.HIGH || severityLevel == SeverityLevel.SEVERE)
                && activeMemberCount != null && activeMemberCount > 10;
    }

    /**
     * Get overall cluster health score
     */
    public double getClusterHealthScore() {
        double health = 0.5; // Base score

        // Improvement rate contributes positively
        if (avgImprovementRate != null && avgImprovementRate > 0) {
            health += avgImprovementRate * 0.3;
        }

        // High retention is good
        if (retentionRate != null) {
            health += retentionRate * 0.2;
        }

        // Good silhouette score
        if (silhouetteScore != null && silhouetteScore > 0) {
            health += silhouetteScore * 0.2;
        }

        return Math.min(1.0, Math.max(0.0, health));
    }

    // Getters and Setters for compatibility
    public ClusterCategory getCategory() {
        return primaryCategory;
    }

    public void setCategory(ClusterCategory category) {
        this.primaryCategory = category;
    }

    public SeverityLevel getLevel() {
        return severityLevel;
    }

    public void setLevel(SeverityLevel level) {
        this.severityLevel = level;
    }
}