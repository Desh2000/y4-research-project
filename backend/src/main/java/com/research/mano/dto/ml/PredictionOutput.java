package com.research.mano.dto.ml;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Output DTO from LSTM Risk Prediction Model (Component 2)
 * Contains prediction results and metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionOutput {

    /**
     * Request identifier for tracking
     */
    private String requestId;

    /**
     * User identifier
     */
    private Long userId;

    /**
     * Timestamp of prediction
     */
    private String timestamp;

    /**
     * Model version used
     */
    private String modelVersion;

    // ==================== PRIMARY PREDICTIONS (0.0 - 1.0) ====================

    /**
     * Stress risk score
     */
    private Double stressScore;

    /**
     * Depression risk score
     */
    private Double depressionScore;

    /**
     * Anxiety risk score
     */
    private Double anxietyScore;

    /**
     * Overall mental health risk score
     */
    private Double overallRiskScore;

    // ==================== CONFIDENCE METRICS ====================

    /**
     * Confidence level for stress prediction
     */
    private Double stressConfidence;

    /**
     * Confidence level for depression prediction
     */
    private Double depressionConfidence;

    /**
     * Confidence level for anxiety prediction
     */
    private Double anxietyConfidence;

    /**
     * Overall model confidence
     */
    private Double overallConfidence;

    // ==================== RISK LEVELS ====================

    /**
     * Categorical risk level for stress
     */
    private RiskLevel stressRiskLevel;

    /**
     * Categorical risk level for depression
     */
    private RiskLevel depressionRiskLevel;

    /**
     * Categorical risk level for anxiety
     */
    private RiskLevel anxietyRiskLevel;

    /**
     * Overall risk level
     */
    private RiskLevel overallRiskLevel;

    // ==================== CLUSTER ASSIGNMENT ====================

    /**
     * Primary cluster category (highest risk)
     */
    private String primaryClusterCategory;

    /**
     * Primary cluster level
     */
    private String primaryClusterLevel;

    /**
     * Full cluster identifier (e.g., "STRESS_HIGH")
     */
    private String clusterIdentifier;

    /**
     * Probability distribution across all clusters
     */
    private Map<String, Double> clusterProbabilities;

    // ==================== TEMPORAL TRENDS ====================

    /**
     * Trend direction for stress (-1: improving, 0: stable, 1: worsening)
     */
    private Integer stressTrend;

    /**
     * Trend direction for depression
     */
    private Integer depressionTrend;

    /**
     * Trend direction for anxiety
     */
    private Integer anxietyTrend;

    /**
     * Predicted scores for next 7 days
     */
    private List<FuturePrediction> futurePredictions;

    // ==================== FEATURE IMPORTANCE ====================

    /**
     * Top contributing features to the prediction
     */
    private List<FeatureImportance> topFeatures;

    /**
     * Attention weights from the temporal attention mechanism
     */
    private Map<String, Double> attentionWeights;

    // ==================== ALERTS AND RECOMMENDATIONS ====================

    /**
     * Whether immediate attention is needed
     */
    private Boolean requiresImmediateAttention;

    /**
     * Crisis indicators detected
     */
    private List<String> crisisIndicators;

    /**
     * Recommended actions based on prediction
     */
    private List<String> recommendations;

    /**
     * Suggested intervention types
     */
    private List<String> suggestedInterventions;

    // ==================== METADATA ====================

    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;

    /**
     * Data quality score (0-1) indicating input completeness
     */
    private Double dataQualityScore;

    /**
     * Warnings or notes about the prediction
     */
    private List<String> warnings;

    /**
     * Whether this is based on synthetic/simulated data
     */
    private Boolean isSimulated;

    // ==================== NESTED CLASSES ====================

    public enum RiskLevel {
        MINIMAL("0-20% risk"),
        LOW("20-40% risk"),
        MODERATE("40-60% risk"),
        HIGH("60-80% risk"),
        SEVERE("80-100% risk");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static RiskLevel fromScore(Double score) {
            if (score == null) return null;
            if (score < 0.2) return MINIMAL;
            if (score < 0.4) return LOW;
            if (score < 0.6) return MODERATE;
            if (score < 0.8) return HIGH;
            return SEVERE;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FuturePrediction {
        private String date;
        private Integer dayOffset;
        private Double predictedStress;
        private Double predictedDepression;
        private Double predictedAnxiety;
        private Double confidence;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeatureImportance {
        private String featureName;
        private Double importance;
        private String category; // temporal, demographic, questionnaire, etc.
        private String direction; // positive/negative impact
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if any score indicates high risk
     */
    public boolean hasHighRisk() {
        return (stressScore != null && stressScore >= 0.7) ||
                (depressionScore != null && depressionScore >= 0.7) ||
                (anxietyScore != null && anxietyScore >= 0.7);
    }

    /**
     * Check if crisis intervention is needed
     */
    public boolean needsCrisisIntervention() {
        return requiresImmediateAttention != null && requiresImmediateAttention ||
                (crisisIndicators != null && !crisisIndicators.isEmpty());
    }

    /**
     * Get the dominant risk category
     */
    public String getDominantCategory() {
        if (stressScore == null || depressionScore == null || anxietyScore == null) {
            return null;
        }

        double max = Math.max(stressScore, Math.max(depressionScore, anxietyScore));
        if (max == stressScore) return "STRESS";
        if (max == depressionScore) return "DEPRESSION";
        return "ANXIETY";
    }

    /**
     * Calculate composite score
     */
    public Double getCompositeScore() {
        if (stressScore == null || depressionScore == null || anxietyScore == null) {
            return null;
        }
        return (stressScore + depressionScore + anxietyScore) / 3.0;
    }
}