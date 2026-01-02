package com.research.mano.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input DTO for GMM Clustering Service (Component 4)
 * Sends user resilience data for peer group assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusteringInput {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("user_id")
    private Long odataId;

    // ==================== RISK SCORES FROM COMPONENT 2 ====================

    @JsonProperty("stress_score")
    private Double stressScore;

    @JsonProperty("depression_score")
    private Double depressionScore;

    @JsonProperty("anxiety_score")
    private Double anxietyScore;

    // ==================== RESILIENCE INDICATORS ====================

    @JsonProperty("resilience_score")
    private Double resilienceScore;

    @JsonProperty("coping_score")
    private Double copingScore;

    @JsonProperty("social_support_score")
    private Double socialSupportScore;

    @JsonProperty("self_efficacy_score")
    private Double selfEfficacyScore;

    @JsonProperty("emotional_regulation_score")
    private Double emotionalRegulationScore;

    // ==================== ENGAGEMENT METRICS ====================

    @JsonProperty("app_engagement_score")
    private Double appEngagementScore;

    @JsonProperty("intervention_adherence")
    private Double interventionAdherence;

    @JsonProperty("community_participation")
    private Double communityParticipation;

    // ==================== DEMOGRAPHICS ====================

    @JsonProperty("age_group")
    private String ageGroup;

    @JsonProperty("primary_concern")
    private String primaryConcern;

    // ==================== OPTIONS ====================

    @JsonProperty("include_recommendations")
    private Boolean includeRecommendations = true;

    @JsonProperty("max_recommendations")
    private Integer maxRecommendations = 5;
}