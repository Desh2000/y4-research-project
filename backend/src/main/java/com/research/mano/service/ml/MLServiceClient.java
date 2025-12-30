package com.research.mano.service.ml;

import com.research.mano.dto.ml.PredictionInput;
import com.research.mano.dto.ml.PredictionOutput;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for ML Service communication
 * Defines contract for interacting with Python ML microservices
 */
public interface MLServiceClient {

    // ==================== HEALTH CHECKS ====================

    /**
     * Check if LSTM prediction service is available
     */
    boolean isLSTMServiceHealthy();

    /**
     * Check if GAN service is available
     */
    boolean isGANServiceHealthy();

    /**
     * Check if Chatbot service is available
     */
    boolean isChatbotServiceHealthy();

    /**
     * Check if Clustering service is available
     */
    boolean isClusteringServiceHealthy();

    /**
     * Get health status of all services
     */
    Map<String, Boolean> getAllServicesHealth();

    // ==================== LSTM PREDICTION (Component 2) ====================

    /**
     * Get mental health risk prediction
     */
    Optional<PredictionOutput> getPrediction(PredictionInput input);

    /**
     * Get batch predictions
     */
    List<PredictionOutput> getBatchPredictions(List<PredictionInput> inputs);

    /**
     * Get prediction with specific model version
     */
    Optional<PredictionOutput> getPredictionWithModel(PredictionInput input, String modelVersion);

    /**
     * Get available model versions
     */
    List<String> getAvailableModelVersions();

    // ==================== GAN SERVICE (Component 1) ====================

    /**
     * Generate synthetic mental health data
     */
    Optional<Map<String, Object>> generateSyntheticData(Map<String, Object> parameters);

    /**
     * Simulate intervention outcome
     */
    Optional<Map<String, Object>> simulateInterventionOutcome(
            Long interventionId,
            Double preStress, Double preDepression, Double preAnxiety,
            Map<String, Object> additionalParams);

    /**
     * Get synthetic data quality metrics
     */
    Optional<Map<String, Object>> getSyntheticDataQuality(String datasetId);

    // ==================== CHATBOT SERVICE (Component 3) ====================

    /**
     * Get chatbot response
     */
    Optional<String> getChatbotResponse(String userId, String message, Map<String, Object> context);

    /**
     * Analyze sentiment of text
     */
    Optional<Map<String, Object>> analyzeSentiment(String text);

    /**
     * Detect crisis indicators in text
     */
    Optional<Map<String, Object>> detectCrisisIndicators(String text);

    // ==================== CLUSTERING SERVICE (Component 4) ====================

    /**
     * Get cluster assignment for user
     */
    Optional<Map<String, Object>> getClusterAssignment(
            Double stressScore, Double depressionScore, Double anxietyScore);

    /**
     * Update cluster model with new data
     */
    boolean updateClusterModel(List<Map<String, Object>> newData);

    /**
     * Get cluster statistics
     */
    Optional<Map<String, Object>> getClusterStatistics();

    // ==================== MODEL MANAGEMENT ====================

    /**
     * Trigger model retraining
     */
    boolean triggerModelRetraining(String serviceType, Map<String, Object> parameters);

    /**
     * Get model performance metrics
     */
    Optional<Map<String, Object>> getModelMetrics(String serviceType);
}