package com.research.mano.service;

import com.research.mano.controller.request.PredictionRequest;
import com.research.mano.controller.responce.PredictionResultDTO;
import com.research.mano.dto.ml.PredictionInput;
import com.research.mano.dto.ml.PredictionOutput;
import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced Prediction Service Interface
 * Bridges between REST API, ML service, and database storage
 */
public interface PredictionService {

    // ==================== PRIMARY PREDICTION OPERATIONS ====================

    /**
     * Create a new prediction from request data
     * Calls ML service and stores result
     */
    PredictionResultDTO createPrediction(Long userId, PredictionRequest request);

    /**
     * Create prediction with direct scores (bypass ML service)
     */
    PredictionResultDTO createDirectPrediction(Long userId, Double stressScore,
                                               Double depressionScore, Double anxietyScore,
                                               String dataSource);

    /**
     * Get prediction from ML service without storing
     */
    Optional<PredictionOutput> getMLPrediction(PredictionInput input);

    /**
     * Re-run prediction with updated model
     */
    PredictionResultDTO rerunPrediction(Long predictionId, String modelVersion);

    // ==================== QUESTIONNAIRE-BASED PREDICTIONS ====================

    /**
     * Create prediction from PHQ-9 responses only
     */
    PredictionResultDTO createFromPHQ9(Long userId, List<Integer> responses);

    /**
     * Create prediction from GAD-7 responses only
     */
    PredictionResultDTO createFromGAD7(Long userId, List<Integer> responses);

    /**
     * Create prediction from PSS responses only
     */
    PredictionResultDTO createFromPSS(Long userId, List<Integer> responses);

    /**
     * Create prediction from all questionnaires
     */
    PredictionResultDTO createFromQuestionnaires(Long userId,
                                                 List<Integer> phq9,
                                                 List<Integer> gad7,
                                                 List<Integer> pss);

    // ==================== RETRIEVAL OPERATIONS ====================

    /**
     * Get prediction by ID
     */
    Optional<PredictionResultDTO> getPredictionById(Long predictionId);

    /**
     * Get user's latest prediction
     */
    Optional<PredictionResultDTO> getLatestPrediction(Long userId);

    /**
     * Get user's prediction history
     */
    List<PredictionResultDTO> getUserPredictionHistory(Long userId);

    /**
     * Get user's prediction history with pagination
     */
    List<PredictionResultDTO> getUserPredictionHistory(Long userId, int page, int size);

    /**
     * Get predictions by date range
     */
    List<PredictionResultDTO> getPredictionsByDateRange(Long userId,
                                                        LocalDateTime start,
                                                        LocalDateTime end);

    // ==================== ANALYSIS OPERATIONS ====================

    /**
     * Get user's risk progression over time
     */
    Map<String, Object> getRiskProgression(Long userId, int days);

    /**
     * Get user's trend analysis
     */
    Map<String, Object> getTrendAnalysis(Long userId);

    /**
     * Compare user's scores with cluster averages
     */
    Map<String, Object> getClusterComparison(Long userId);

    /**
     * Get prediction statistics for a user
     */
    Map<String, Object> getUserPredictionStatistics(Long userId);

    // ==================== CLUSTER OPERATIONS ====================

    /**
     * Get cluster assignment for scores
     */
    Map<String, Object> getClusterAssignment(Double stressScore,
                                             Double depressionScore,
                                             Double anxietyScore);

    /**
     * Update user's cluster based on latest prediction
     */
    void updateUserCluster(Long userId, Long predictionId);

    // ==================== ALERT OPERATIONS ====================

    /**
     * Check if prediction requires immediate attention
     */
    boolean requiresImmediateAttention(Long predictionId);

    /**
     * Get high-risk predictions for monitoring
     */
    List<PredictionResultDTO> getHighRiskPredictions(Double threshold);

    /**
     * Get predictions requiring follow-up
     */
    List<PredictionResultDTO> getPredictionsRequiringFollowUp();

    // ==================== ML SERVICE OPERATIONS ====================

    /**
     * Check ML service health
     */
    Map<String, Boolean> checkMLServiceHealth();

    /**
     * Get available model versions
     */
    List<String> getAvailableModelVersions();

    /**
     * Get model performance metrics
     */
    Map<String, Object> getModelMetrics();

    // ==================== BATCH OPERATIONS ====================

    /**
     * Create batch predictions
     */
    List<PredictionResultDTO> createBatchPredictions(List<PredictionRequest> requests, Long userId);

    /**
     * Recalculate predictions for a user
     */
    List<PredictionResultDTO> recalculateUserPredictions(Long userId, String modelVersion);
}