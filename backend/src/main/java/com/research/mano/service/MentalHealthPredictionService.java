package com.research.mano.service;


import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mental Health Prediction Service Interface
 * Business logic for Component 2 (LSTM Prediction System)
 */
public interface MentalHealthPredictionService extends BaseService<MentalHealthPrediction, Long> {

    /**
     * Create a new prediction record
     */
    MentalHealthPrediction createPrediction(User user, Double stressScore, Double depressionScore,
                                            Double anxietyScore, String modelVersion, String dataSource);

    /**
     * Get the latest prediction for user
     */
    Optional<MentalHealthPrediction> getLatestPrediction(User user);

    /**
     * Get latest prediction by user ID
     */
    Optional<MentalHealthPrediction> getLatestPrediction(Long userId);

    /**
     * Get all predictions for user
     */
    List<MentalHealthPrediction> getAllPredictions(User user);

    /**
     * Get predictions for user within date range
     */
    List<MentalHealthPrediction> getPredictionsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find high-risk predictions (any score >= 0.8)
     */
    List<MentalHealthPrediction> findHighRiskPredictions();

    /**
     * Find predictions by cluster category
     */
    List<MentalHealthPrediction> findByClusterCategory(MentalHealthPrediction.ClusterCategory category);

    /**
     * Find predictions by cluster level
     */
    List<MentalHealthPrediction> findByClusterLevel(MentalHealthPrediction.ClusterLevel level);

    /**
     * Find predictions needing cluster assignment
     */
    List<MentalHealthPrediction> findPredictionsNeedingClustering();

    /**
     * Update prediction with cluster assignment
     */
    MentalHealthPrediction assignCluster(Long predictionId,
                                         MentalHealthPrediction.ClusterCategory category,
                                         MentalHealthPrediction.ClusterLevel level);

    /**
     * Batch process predictions for clustering (Component 4 integration)
     */
    List<MentalHealthPrediction> processNewPredictionsForClustering();

    /**
     * Get prediction statistics by cluster
     */
    List<Object[]> getPredictionStatsByCluster();

    /**
     * Get daily prediction counts
     */
    List<Object[]> getDailyPredictionCounts(LocalDateTime startDate);

    /**
     * Find trending risk increases
     */
    List<MentalHealthPrediction> findTrendingRiskIncreases();

    /**
     * Get user risk progression
     */
    List<Object[]> getUserRiskProgression(Long userId);

    /**
     * Find predictions for ML model retraining
     */
    List<MentalHealthPrediction> findPredictionsForMLRetraining(int daysBack);

    /**
     * Update prediction scores (for model corrections)
     */
    MentalHealthPrediction updatePredictionScores(Long predictionId, Double stressScore,
                                                  Double depressionScore, Double anxietyScore);

    /**
     * Calculate and update overall risk score
     */
    MentalHealthPrediction calculateOverallRisk(Long predictionId);

    /**
     * Find predictions by model version
     */
    List<MentalHealthPrediction> findByModelVersion(String modelVersion);

    /**
     * Generate alerts for high-risk predictions
     */
    void generateHighRiskAlerts(List<MentalHealthPrediction> predictions);

    /**
     * Validate prediction scores (0.0-1.0 range)
     */
    boolean validatePredictionScores(Double stressScore, Double depressionScore, Double anxietyScore);

    /**
     * Get average scores by time period
     */
    List<Object[]> getAverageScoresByDate(LocalDateTime startDate);
}