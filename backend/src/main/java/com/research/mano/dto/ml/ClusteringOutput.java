package com.research.mano.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Output DTO from GMM Clustering Service (Component 4)
 * Contains peer group assignment and activity recommendations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusteringOutput {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("user_id")
    private Long userId;

    // ==================== CLUSTER ASSIGNMENT ====================

    @JsonProperty("cluster_id")
    private Integer clusterId;

    @JsonProperty("cluster_identifier")
    private String clusterIdentifier;

    @JsonProperty("cluster_name")
    private String clusterName;

    @JsonProperty("cluster_category")
    private String clusterCategory;

    @JsonProperty("severity_level")
    private String severityLevel;

    @JsonProperty("cluster_description")
    private String clusterDescription;

    // ==================== MEMBERSHIP PROBABILITIES ====================

    @JsonProperty("membership_probability")
    private Double membershipProbability;

    @JsonProperty("all_probabilities")
    private Map<String, Double> allProbabilities;

    @JsonProperty("is_boundary_case")
    private Boolean isBoundaryCase;

    // ==================== CLUSTER STATISTICS ====================

    @JsonProperty("cluster_size")
    private Integer clusterSize;

    @JsonProperty("cluster_avg_stress")
    private Double clusterAvgStress;

    @JsonProperty("cluster_avg_resilience")
    private Double clusterAvgResilience;

    // ==================== COMPUTED SCORES ====================

    @JsonProperty("stress_level")
    private String stressLevel;

    @JsonProperty("resilience_level")
    private String resilienceLevel;

    @JsonProperty("overall_wellbeing_score")
    private Double overallWellbeingScore;

    // ==================== ACTIVITY RECOMMENDATIONS ====================

    @JsonProperty("recommended_activities")
    private List<ActivityRecommendation> recommendedActivities;

    // ==================== PEER GROUP INFO ====================

    @JsonProperty("peer_group_id")
    private String peerGroupId;

    @JsonProperty("peer_group_size")
    private Integer peerGroupSize;

    @JsonProperty("peer_match_score")
    private Double peerMatchScore;

    // ==================== METADATA ====================

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    // ==================== NESTED CLASSES ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityRecommendation {

        @JsonProperty("activity_id")
        private String activityId;

        @JsonProperty("activity_name")
        private String activityName;

        @JsonProperty("category")
        private String category;

        @JsonProperty("description")
        private String description;

        @JsonProperty("duration_minutes")
        private Integer durationMinutes;

        @JsonProperty("difficulty_level")
        private String difficultyLevel;

        @JsonProperty("relevance_score")
        private Double relevanceScore;

        @JsonProperty("expected_benefit")
        private String expectedBenefit;
    }
}