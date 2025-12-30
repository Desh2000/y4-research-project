package com.research.mano.controller.responce;

import com.research.mano.dto.ml.PredictionOutput;
import com.research.mano.entity.MentalHealthPrediction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive prediction result DTO
 * Combines stored prediction with ML service output
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResultDTO {

    // ==================== IDENTIFICATION ====================

    private Long predictionId;
    private String requestId;
    private Long userId;
    private LocalDateTime predictionDate;
    private String modelVersion;
    private String dataSource;

    // ==================== PRIMARY SCORES (0.0-1.0) ====================

    private Double stressScore;
    private Double depressionScore;
    private Double anxietyScore;
    private Double overallRiskScore;

    // ==================== RISK LEVELS ====================

    private String stressRiskLevel;
    private String depressionRiskLevel;
    private String anxietyRiskLevel;
    private String overallRiskLevel;

    // ==================== CONFIDENCE ====================

    private Double stressConfidence;
    private Double depressionConfidence;
    private Double anxietyConfidence;
    private Double overallConfidence;

    // ==================== CLUSTER ASSIGNMENT ====================

    private String primaryClusterCategory;
    private String primaryClusterLevel;
    private String clusterIdentifier;
    private Map<String, Double> clusterProbabilities;

    // ==================== TRENDS ====================

    private String stressTrend; // IMPROVING, STABLE, WORSENING
    private String depressionTrend;
    private String anxietyTrend;
    private List<FuturePredictionDTO> futurePredictions;

    // ==================== FEATURE ANALYSIS ====================

    private List<FeatureImportanceDTO> topFeatures;
    private Map<String, Double> attentionWeights;

    // ==================== RECOMMENDATIONS ====================

    private Boolean requiresImmediateAttention;
    private List<String> crisisIndicators;
    private List<String> recommendations;
    private List<String> suggestedInterventionTypes;

    // ==================== QUESTIONNAIRE SCORES ====================

    private Integer phq9TotalScore;
    private String phq9Severity; // Minimal, Mild, Moderate, Moderately Severe, Severe
    private Integer gad7TotalScore;
    private String gad7Severity;
    private Integer pssTotalScore;
    private String pssSeverity;

    // ==================== METADATA ====================

    private Long processingTimeMs;
    private Double dataQualityScore;
    private List<String> warnings;
    private Boolean isSimulated;
    private LocalDateTime createdAt;

    // ==================== NESTED CLASSES ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FuturePredictionDTO {
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
    public static class FeatureImportanceDTO {
        private String featureName;
        private Double importance;
        private String category;
        private String impact; // POSITIVE or NEGATIVE
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Create from MentalHealthPrediction entity
     */
    public static PredictionResultDTO fromEntity(MentalHealthPrediction entity) {
        if (entity == null) return null;

        return PredictionResultDTO.builder()
                .predictionId(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .predictionDate(entity.getPredictionDate())
                .modelVersion(entity.getModelVersion())
                .dataSource(entity.getDataSource())
                .stressScore(entity.getStressScore())
                .depressionScore(entity.getDepressionScore())
                .anxietyScore(entity.getAnxietyScore())
                .overallRiskScore(entity.getOverallRiskScore())
                .stressRiskLevel(getRiskLevel(entity.getStressScore()))
                .depressionRiskLevel(getRiskLevel(entity.getDepressionScore()))
                .anxietyRiskLevel(getRiskLevel(entity.getAnxietyScore()))
                .overallRiskLevel(getRiskLevel(entity.getOverallRiskScore()))
                .primaryClusterCategory(entity.getPrimaryClusterCategory() != null
                        ? entity.getPrimaryClusterCategory().name() : null)
                .primaryClusterLevel(entity.getPrimaryClusterLevel() != null
                        ? entity.getPrimaryClusterLevel().name() : null)
                .clusterIdentifier(entity.getClusterIdentifier())
                .requiresImmediateAttention(isHighRisk(entity))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Create from ML service output
     */
    public static PredictionResultDTO fromMLOutput(PredictionOutput output, Long predictionId) {
        if (output == null) return null;

        PredictionResultDTO dto = PredictionResultDTO.builder()
                .predictionId(predictionId)
                .requestId(output.getRequestId())
                .userId(output.getUserId())
                .predictionDate(LocalDateTime.now())
                .modelVersion(output.getModelVersion())
                .stressScore(output.getStressScore())
                .depressionScore(output.getDepressionScore())
                .anxietyScore(output.getAnxietyScore())
                .overallRiskScore(output.getOverallRiskScore())
                .stressConfidence(output.getStressConfidence())
                .depressionConfidence(output.getDepressionConfidence())
                .anxietyConfidence(output.getAnxietyConfidence())
                .overallConfidence(output.getOverallConfidence())
                .primaryClusterCategory(output.getPrimaryClusterCategory())
                .primaryClusterLevel(output.getPrimaryClusterLevel())
                .clusterIdentifier(output.getClusterIdentifier())
                .clusterProbabilities(output.getClusterProbabilities())
                .requiresImmediateAttention(output.getRequiresImmediateAttention())
                .crisisIndicators(output.getCrisisIndicators())
                .recommendations(output.getRecommendations())
                .suggestedInterventionTypes(output.getSuggestedInterventions())
                .processingTimeMs(output.getProcessingTimeMs())
                .dataQualityScore(output.getDataQualityScore())
                .warnings(output.getWarnings())
                .isSimulated(output.getIsSimulated())
                .build();

        // Set risk levels
        if (output.getStressRiskLevel() != null) {
            dto.setStressRiskLevel(output.getStressRiskLevel().name());
        }
        if (output.getDepressionRiskLevel() != null) {
            dto.setDepressionRiskLevel(output.getDepressionRiskLevel().name());
        }
        if (output.getAnxietyRiskLevel() != null) {
            dto.setAnxietyRiskLevel(output.getAnxietyRiskLevel().name());
        }
        if (output.getOverallRiskLevel() != null) {
            dto.setOverallRiskLevel(output.getOverallRiskLevel().name());
        }

        // Set trends
        if (output.getStressTrend() != null) {
            dto.setStressTrend(getTrendLabel(output.getStressTrend()));
        }
        if (output.getDepressionTrend() != null) {
            dto.setDepressionTrend(getTrendLabel(output.getDepressionTrend()));
        }
        if (output.getAnxietyTrend() != null) {
            dto.setAnxietyTrend(getTrendLabel(output.getAnxietyTrend()));
        }

        // Convert future predictions
        if (output.getFuturePredictions() != null) {
            dto.setFuturePredictions(output.getFuturePredictions().stream()
                    .map(fp -> FuturePredictionDTO.builder()
                            .date(fp.getDate())
                            .dayOffset(fp.getDayOffset())
                            .predictedStress(fp.getPredictedStress())
                            .predictedDepression(fp.getPredictedDepression())
                            .predictedAnxiety(fp.getPredictedAnxiety())
                            .confidence(fp.getConfidence())
                            .build())
                    .toList());
        }

        // Convert feature importance
        if (output.getTopFeatures() != null) {
            dto.setTopFeatures(output.getTopFeatures().stream()
                    .map(fi -> FeatureImportanceDTO.builder()
                            .featureName(fi.getFeatureName())
                            .importance(fi.getImportance())
                            .category(fi.getCategory())
                            .impact(fi.getDirection())
                            .build())
                    .toList());
        }

        dto.setAttentionWeights(output.getAttentionWeights());

        return dto;
    }

    // ==================== HELPER METHODS ====================

    private static String getRiskLevel(Double score) {
        if (score == null) return "UNKNOWN";
        if (score < 0.2) return "MINIMAL";
        if (score < 0.4) return "LOW";
        if (score < 0.6) return "MODERATE";
        if (score < 0.8) return "HIGH";
        return "SEVERE";
    }

    private static boolean isHighRisk(MentalHealthPrediction prediction) {
        return (prediction.getStressScore() != null && prediction.getStressScore() >= 0.7) ||
                (prediction.getDepressionScore() != null && prediction.getDepressionScore() >= 0.7) ||
                (prediction.getAnxietyScore() != null && prediction.getAnxietyScore() >= 0.7);
    }

    private static String getTrendLabel(Integer trend) {
        if (trend == null) return "UNKNOWN";
        if (trend < 0) return "IMPROVING";
        if (trend > 0) return "WORSENING";
        return "STABLE";
    }

    /**
     * Get PHQ-9 severity label
     */
    public static String getPHQ9Severity(Integer score) {
        if (score == null) return null;
        if (score <= 4) return "Minimal";
        if (score <= 9) return "Mild";
        if (score <= 14) return "Moderate";
        if (score <= 19) return "Moderately Severe";
        return "Severe";
    }

    /**
     * Get GAD-7 severity label
     */
    public static String getGAD7Severity(Integer score) {
        if (score == null) return null;
        if (score <= 4) return "Minimal";
        if (score <= 9) return "Mild";
        if (score <= 14) return "Moderate";
        return "Severe";
    }

    /**
     * Get PSS severity label
     */
    public static String getPSSSeverity(Integer score) {
        if (score == null) return null;
        if (score <= 13) return "Low";
        if (score <= 26) return "Moderate";
        return "High";
    }
}