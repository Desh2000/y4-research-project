package com.research.mano.service.ml;

import com.research.mano.dto.ml.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ML Service Client Interface
 * Defines communication contract with Python ML microservices
 * Supports both legacy and new API patterns for backward compatibility
 */
public interface MLServiceClient {

    // ==================== HEALTH CHECKS ====================

    /**
     * Check if all ML services are healthy (new API)
     */
    Map<String, Boolean> checkAllServicesHealth();

    /**
     * Get health status of all services (legacy API - alias)
     */
    default Map<String, Boolean> getAllServicesHealth() {
        return checkAllServicesHealth();
    }

    /**
     * Check LSTM prediction service health (legacy naming)
     */
    boolean isLSTMServiceHealthy();

    /**
     * Check LSTM prediction service health (new naming)
     */
    default boolean isLstmServiceHealthy() {
        return isLSTMServiceHealthy();
    }

    Optional<String> getChatbotResponse(String odataId, String message, Map<String, Object> context);
    /**
     * Check GAN service health (legacy naming)
     */
    boolean isGANServiceHealthy();

    /**
     * Check GAN service health (new naming)
     */
    default boolean isGanServiceHealthy() {
        return isGANServiceHealthy();
    }

    /**
     * Check Chatbot service health
     */
    boolean isChatbotServiceHealthy();

    /**
     * Check Clustering service health
     */
    boolean isClusteringServiceHealthy();

    // ==================== COMPONENT 2: LSTM PREDICTIONS ====================

    /**
     * Get mental health risk prediction (new API)
     * @param input User data for prediction
     * @return Prediction results with risk scores
     */
    PredictionOutput predictRisk(PredictionInput input);

    /**
     * Get mental health risk prediction (legacy API)
     * @param input User data for prediction
     * @return Optional containing prediction results
     */
    default Optional<PredictionOutput> getPrediction(PredictionInput input) {
        try {
            PredictionOutput result = predictRisk(input);
            return result != null ? Optional.of(result) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get batch predictions for multiple users (new API)
     * @param inputs List of user data
     * @return List of prediction results
     */
    List<PredictionOutput> predictRiskBatch(List<PredictionInput> inputs);

    /**
     * Get batch predictions (legacy API)
     */
    default List<PredictionOutput> getBatchPredictions(List<PredictionInput> inputs) {
        return predictRiskBatch(inputs);
    }

    /**
     * Get prediction with specific model version (legacy API)
     */
    default Optional<PredictionOutput> getPredictionWithModel(PredictionInput input, String modelVersion) {
        // Set model version in input if needed
        return getPrediction(input);
    }

    /**
     * Get available model versions (legacy API)
     */
    List<String> getAvailableModelVersions();

    // ==================== COMPONENT 4: GMM CLUSTERING ====================

    /**
     * Assign user to a peer support cluster (new API)
     * @param input User resilience data
     * @return Cluster assignment and recommendations
     */
    ClusteringOutput assignCluster(ClusteringInput input);

    /**
     * Get cluster analysis for multiple users (new API)
     * @param inputs List of user data
     * @return List of cluster assignments
     */
    List<ClusteringOutput> assignClusterBatch(List<ClusteringInput> inputs);

    /**
     * Get all cluster statistics (new API)
     * @return Map of cluster ID to statistics
     */
    Map<String, Object> getClusterStatistics();

    /**
     * Get cluster assignment (legacy API)
     */
    default Optional<Map<String, Object>> getClusterAssignment(
            Double stressScore, Double depressionScore, Double anxietyScore) {
        try {
            ClusteringInput input = ClusteringInput.builder()
                    .stressScore(stressScore)
                    .depressionScore(depressionScore)
                    .anxietyScore(anxietyScore)
                    .build();

            ClusteringOutput output = assignCluster(input);
            if (output == null) return Optional.empty();

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("clusterId", output.getClusterId());
            result.put("clusterIdentifier", output.getClusterIdentifier());
            result.put("clusterName", output.getClusterName());
            result.put("membershipProbability", output.getMembershipProbability());
            result.put("stressLevel", output.getStressLevel());
            result.put("resilienceLevel", output.getResilienceLevel());
            result.put("primaryCategory", output.getClusterCategory());
            result.put("primaryLevel", output.getSeverityLevel());
            result.put("confidence", output.getMembershipProbability());
            result.put("modelVersion", output.getModelVersion());
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== COMPONENT 3: CHATBOT ====================

    /**
     * Generate empathetic chatbot response (new API)
     * @param input User message and context
     * @return Chatbot response with sentiment and crisis detection
     */
    ChatbotOutput generateChatResponse(ChatbotInput input);

    /**
     * Get chatbot response (legacy API)
     */
    default Optional<String> getChatbotResponse(Long userId, String message, Map<String, Object> context) {
        try {
            ChatbotInput input = ChatbotInput.builder()
                    .userId(userId)
                    .message(message)
                    .build();

            ChatbotOutput output = generateChatResponse(input);
            return output != null ? Optional.ofNullable(output.getResponse()) : Optional.empty();
        } catch (Exception e) {
            return Optional.of("I'm having trouble responding right now. Please try again later.");
        }
    }

    /**
     * Analyze sentiment of a message (new API)
     * @param message Text to analyze
     * @return Sentiment analysis result
     */
    ChatbotOutput.SentimentResult analyzeSentimentNew(String message);

    /**
     * Analyze sentiment (legacy API returning Map)
     */
    default Optional<Map<String, Object>> analyzeSentiment(String message) {
        try {
            ChatbotOutput.SentimentResult result = analyzeSentimentNew(message);
            if (result == null) return Optional.empty();

            Map<String, Object> map = new java.util.HashMap<>();
            map.put("sentiment", result.getSentiment());
            map.put("score", result.getSentimentScore());
            map.put("confidence", result.getConfidence());
            map.put("valence", result.getValence());
            map.put("arousal", result.getArousal());
            return Optional.of(map);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Check message for crisis indicators (new API)
     * @param message Text to check
     * @param userId User ID for context
     * @return Crisis detection result
     */
    CrisisDetectionResult detectCrisis(String message, Long userId);

    /**
     * Detect crisis indicators (legacy API)
     */
    default Optional<Map<String, Object>> detectCrisisIndicators(String text) {
        try {
            CrisisDetectionResult result = detectCrisis(text, null);
            if (result == null) return Optional.empty();

            Map<String, Object> map = new java.util.HashMap<>();
            map.put("crisisDetected", result.getCrisisDetected());
            map.put("crisisLevel", result.getCrisisLevel());
            map.put("crisisType", result.getCrisisType());
            map.put("keywords", result.getKeywords());
            map.put("recommendedAction", result.getRecommendedAction());
            return Optional.of(map);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== COMPONENT 1: GAN SYNTHETIC DATA ====================

    /**
     * Generate synthetic mental health data (new API)
     * @param request Generation parameters
     * @return Generated synthetic data
     */
    SyntheticDataOutput generateSyntheticData(SyntheticDataRequest request);

    /**
     * Generate synthetic data (legacy API)
     */
    default Optional<Map<String, Object>> generateSyntheticData(Map<String, Object> parameters) {
        try {
            SyntheticDataRequest request = SyntheticDataRequest.builder()
                    .requestId(java.util.UUID.randomUUID().toString())
                    .recordCount((Integer) parameters.getOrDefault("recordCount", 100))
                    .generationMethod((String) parameters.getOrDefault("method", "CTGAN"))
                    .build();

            SyntheticDataOutput output = generateSyntheticData(request);
            if (output == null) return Optional.empty();

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("recordId", output.getRecordId());
            result.put("recordsGenerated", output.getRecordsGenerated());
            result.put("privacyScore", output.getPrivacyScore());
            result.put("utilityScore", output.getUtilityScore());
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Simulate intervention outcomes (new API)
     * @param request Simulation parameters
     * @return Simulated outcomes
     */
    InterventionSimulationOutput simulateIntervention(InterventionSimulationRequest request);

    /**
     * Simulate intervention outcome (legacy API)
     */
    default Optional<Map<String, Object>> simulateInterventionOutcome(
            Long interventionId,
            Double preStress, Double preDepression, Double preAnxiety,
            Map<String, Object> additionalParams) {
        try {
            Map<String, Double> baselineScores = new java.util.HashMap<>();
            baselineScores.put("stress", preStress);
            baselineScores.put("depression", preDepression);
            baselineScores.put("anxiety", preAnxiety);

            InterventionSimulationRequest request = InterventionSimulationRequest.builder()
                    .requestId(java.util.UUID.randomUUID().toString())
                    .baselineScores(baselineScores)
                    .build();

            InterventionSimulationOutput output = simulateIntervention(request);
            if (output == null) return Optional.empty();

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("simulationId", output.getSimulationId());
            result.put("predictedOutcomes", output.getPredictedOutcomes());
            result.put("expectedImprovement", output.getExpectedImprovement());
            result.put("confidenceScore", output.getConfidenceScore());
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get synthetic data quality metrics (legacy API)
     */
    default Optional<Map<String, Object>> getSyntheticDataQuality(String datasetId) {
        // This would need a separate endpoint in the ML service
        return Optional.empty();
    }

    // ==================== MODEL MANAGEMENT ====================

    /**
     * Trigger model retraining
     */
    boolean triggerModelRetraining(String serviceType, Map<String, Object> parameters);

    /**
     * Get model performance metrics
     */
    Optional<Map<String, Object>> getModelMetrics(String serviceType);

    // ==================== INNER CLASSES FOR DTOs ====================

    /**
     * Crisis detection result
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class CrisisDetectionResult {
        private Boolean crisisDetected;
        private String crisisLevel;
        private String crisisType;
        private List<String> keywords;
        private String recommendedAction;
        private Boolean requiresImmediateIntervention;
    }

    /**
     * Request for synthetic data generation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class SyntheticDataRequest {
        private String requestId;
        private String generationMethod;
        private Integer recordCount;
        private String sourceCluster;
        private Double privacyBudgetEpsilon;
        private Double privacyBudgetDelta;
        private Map<String, Object> generationParameters;
    }

    /**
     * Output from synthetic data generation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class SyntheticDataOutput {
        private String requestId;
        private String recordId;
        private String generationMethod;
        private Integer recordsGenerated;
        private Double privacyScore;
        private Double utilityScore;
        private Double fidelityScore;
        private String dataLocation;
        private Map<String, Object> metadata;
    }

    /**
     * Request for intervention simulation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class InterventionSimulationRequest {
        private String requestId;
        private Long userId;
        private String interventionType;
        private Integer durationWeeks;
        private Double intensity;
        private Map<String, Double> baselineScores;
        private String simulationModel;
    }

    /**
     * Output from intervention simulation
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class InterventionSimulationOutput {
        private String requestId;
        private String simulationId;
        private Map<String, Double> predictedOutcomes;
        private Double expectedImprovement;
        private Double confidenceScore;
        private List<String> riskFactors;
        private String recommendedAdjustments;
        private Map<String, Object> simulationDetails;
    }
}