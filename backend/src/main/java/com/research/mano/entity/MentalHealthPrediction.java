package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Mental Health Prediction Entity
 * Stores prediction scores from Component 2 (LSTM Model)
 * Scores range from 0.0 to 1.0 for Stress, Depression, and Anxiety
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mental_health_predictions")
public class MentalHealthPrediction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Prediction scores from LSTM model (0.0 - 1.0 range)
    @Column(name = "stress_score", nullable = false)
    private Double stressScore;

    @Column(name = "depression_score", nullable = false)
    private Double depressionScore;

    @Column(name = "anxiety_score", nullable = false)
    private Double anxietyScore;

    // Overall risk assessment
    @Column(name = "overall_risk_score")
    private Double overallRiskScore;

    @Column(name = "prediction_date", nullable = false)
    private LocalDateTime predictionDate;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "confidence_level")
    private Double confidenceLevel;

    // Data source information
    @Column(name = "data_source")
    private String dataSource; // e.g., "questionnaire", "behavioral_data", "text_analysis"

    @Column(name = "input_features", columnDefinition = "TEXT")
    private String inputFeatures; // JSON string of input features used

    // Clustering assignment (will be set by Component 4)
    @Column(name = "primary_cluster_category")
    @Enumerated(EnumType.STRING)
    private ClusterCategory primaryClusterCategory;

    @Column(name = "primary_cluster_level")
    @Enumerated(EnumType.STRING)
    private ClusterLevel primaryClusterLevel;

    @Column(name = "cluster_assignment_date")
    private LocalDateTime clusterAssignmentDate;

    // Constructors
    public MentalHealthPrediction() {
        super();
        this.predictionDate = LocalDateTime.now();
    }

    public MentalHealthPrediction(User user, Double stressScore, Double depressionScore, Double anxietyScore) {
        this();
        this.user = user;
        this.stressScore = stressScore;
        this.depressionScore = depressionScore;
        this.anxietyScore = anxietyScore;
        this.overallRiskScore = calculateOverallRisk();
    }



    // Utility Methods
    public Double calculateOverallRisk() {
        if (stressScore == null || depressionScore == null || anxietyScore == null) {
            return null;
        }
        return (stressScore + depressionScore + anxietyScore) / 3.0;
    }

    public ClusterCategory getDominantCategory() {
        if (stressScore == null || depressionScore == null || anxietyScore == null) {
            return null;
        }

        Double maxScore = Math.max(stressScore, Math.max(depressionScore, anxietyScore));

        if (maxScore.equals(stressScore)) {
            return ClusterCategory.STRESS;
        } else if (maxScore.equals(depressionScore)) {
            return ClusterCategory.DEPRESSION;
        } else {
            return ClusterCategory.ANXIETY;
        }
    }

    public ClusterLevel getScoreLevel(Double score) {
        if (score == null) return null;

        if (score >= 0.1 && score <= 0.3) {
            return ClusterLevel.LOW;
        } else if (score >= 0.4 && score <= 0.7) {
            return ClusterLevel.MEDIUM;
        } else if (score >= 0.8 && score <= 1.0) {
            return ClusterLevel.HIGH;
        }
        return null; // For scores outside expected ranges
    }

    public void assignCluster() {
        this.primaryClusterCategory = getDominantCategory();
        if (primaryClusterCategory != null) {
            Double dominantScore = switch (primaryClusterCategory) {
                case STRESS -> stressScore;
                case DEPRESSION -> depressionScore;
                case ANXIETY -> anxietyScore;
            };
            this.primaryClusterLevel = getScoreLevel(dominantScore);
            this.clusterAssignmentDate = LocalDateTime.now();
        }
    }

    public String getClusterIdentifier() {
        if (primaryClusterCategory == null || primaryClusterLevel == null) {
            return null;
        }
        return primaryClusterCategory.name() + "_" + primaryClusterLevel.name();
    }

    @Override
    public String toString() {
        return "MentalHealthPrediction{" +
                "id=" + getId() +
                ", userId=" + (user != null ? user.getId() : null) +
                ", stressScore=" + stressScore +
                ", depressionScore=" + depressionScore +
                ", anxietyScore=" + anxietyScore +
                ", overallRisk=" + overallRiskScore +
                ", cluster=" + getClusterIdentifier() +
                '}';
    }

    /**
     * Enum for Cluster Categories (3 main categories)
     */
    public enum ClusterCategory {
        STRESS("Stress-related mental health concerns"),
        DEPRESSION("Depression-related mental health concerns"),
        ANXIETY("Anxiety-related mental health concerns");

        private final String description;

        ClusterCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enum for Cluster Levels (3 levels per category)
     */
    public enum ClusterLevel {
        LOW(1, 0.1, 0.3, "Low risk level"),
        MEDIUM(2, 0.4, 0.7, "Medium risk level"),
        HIGH(3, 0.8, 1.0, "High risk level");

        private final int clusterNumber;
        private final double minScore;
        private final double maxScore;
        private final String description;

        ClusterLevel(int clusterNumber, double minScore, double maxScore, String description) {
            this.clusterNumber = clusterNumber;
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.description = description;
        }

        public int getClusterNumber() {
            return clusterNumber;
        }

        public double getMinScore() {
            return minScore;
        }

        public double getMaxScore() {
            return maxScore;
        }

        public String getDescription() {
            return description;
        }

        public boolean isInRange(double score) {
            return score >= minScore && score <= maxScore;
        }
    }
}