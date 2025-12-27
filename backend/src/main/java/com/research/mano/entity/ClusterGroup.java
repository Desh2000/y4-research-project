package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Cluster Group Entity for Component 4 (GMM Clustering)
 * Represents resilience clusters based on prediction scores
 * 9 Total Clusters: 3 Categories × 3 Levels each
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cluster_groups")
public class ClusterGroup extends BaseEntity {

    @Column(name = "cluster_identifier", unique = true, nullable = false)
    private String clusterIdentifier; // e.g., "STRESS_LOW", "DEPRESSION_MEDIUM", "ANXIETY_HIGH"

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private MentalHealthPrediction.ClusterCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private MentalHealthPrediction.ClusterLevel level;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "cluster_description", columnDefinition = "TEXT")
    private String clusterDescription;

    // GMM Model parameters
    @Column(name = "centroid_stress")
    private Double centroidStress;

    @Column(name = "centroid_depression")
    private Double centroidDepression;

    @Column(name = "centroid_anxiety")
    private Double centroidAnxiety;

    @Column(name = "covariance_matrix", columnDefinition = "TEXT")
    private String covarianceMatrix; // JSON representation

    @Column(name = "cluster_weight")
    private Double clusterWeight; // GMM mixture weight

    // Cluster statistics
    @Column(name = "member_count")
    private Integer memberCount = 0;

    @Column(name = "average_resilience_score")
    private Double averageResilienceScore;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "model_version")
    private String modelVersion;

    // Support recommendations for this cluster
    @Column(name = "recommended_interventions", columnDefinition = "TEXT")
    private String recommendedInterventions; // JSON array

    @Column(name = "peer_support_activities", columnDefinition = "TEXT")
    private String peerSupportActivities; // JSON array

    @Column(name = "professional_support_level")
    @Enumerated(EnumType.STRING)
    private ProfessionalSupportLevel professionalSupportLevel;

    // Constructors
    public ClusterGroup() {
        super();
        this.lastUpdated = LocalDateTime.now();
    }

    public ClusterGroup(MentalHealthPrediction.ClusterCategory category,
                        MentalHealthPrediction.ClusterLevel level) {
        this();
        this.category = category;
        this.level = level;
        this.clusterIdentifier = category.name() + "_" + level.name();
        this.clusterName = generateClusterName();
        this.clusterDescription = generateClusterDescription();
        this.professionalSupportLevel = determineProfessionalSupportLevel();
    }


    // Utility Methods
    private String generateClusterName() {
        if (level == null || category == null) return "";
        return level.name().toLowerCase() + " " + category.name().toLowerCase() + " cluster";
    }

    private String generateClusterDescription() {
        if (level == null || category == null) return "";
        return String.format("%s level %s with scores between %.1f-%.1f",
                level.getDescription(),
                category.getDescription().toLowerCase(),
                level.getMinScore(),
                level.getMaxScore());
    }

    private ProfessionalSupportLevel determineProfessionalSupportLevel() {
        if (level == null) return null;
        return switch (level) {
            case LOW -> ProfessionalSupportLevel.MINIMAL;
            case MEDIUM -> ProfessionalSupportLevel.MODERATE;
            case HIGH -> ProfessionalSupportLevel.INTENSIVE;
        };
    }

    public void incrementMemberCount() {
        this.memberCount = (this.memberCount == null) ? 1 : this.memberCount + 1;
        this.lastUpdated = LocalDateTime.now();
    }

    public void decrementMemberCount() {
        this.memberCount = (this.memberCount == null || this.memberCount <= 0) ? 0 : this.memberCount - 1;
        this.lastUpdated = LocalDateTime.now();
    }

    public boolean isEmpty() {
        return memberCount == null || memberCount == 0;
    }

    @Override
    public String toString() {
        return "ClusterGroup{" +
                "id=" + getId() +
                ", identifier='" + clusterIdentifier + '\'' +
                ", category=" + category +
                ", level=" + level +
                ", members=" + memberCount +
                '}';
    }

    /**
     * Professional Support Level Enum
     */
    public enum ProfessionalSupportLevel {
        MINIMAL("Peer support and self-help resources"),
        MODERATE("Regular check-ins with mental health professionals"),
        INTENSIVE("Immediate professional intervention and intensive support");

        private final String description;

        ProfessionalSupportLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}