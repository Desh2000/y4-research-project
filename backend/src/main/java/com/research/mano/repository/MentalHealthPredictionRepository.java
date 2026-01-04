package com.research.mano.repository;

import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.List;

/**
 * Mental Health Prediction Repository Interface
 * Handles CRUD operations for Component 2 (LSTM) predictions
 */
@Repository
public interface MentalHealthPredictionRepository extends BaseRepository<MentalHealthPrediction, Long> {

    /**
     * Find the latest prediction for a user
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.user = :user ORDER BY p.predictionDate DESC")
    Optional<MentalHealthPrediction> findLatestByUser(@Param("user") User user);

    /**
     * Find the latest prediction by user ID
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.user.id = :userId ORDER BY p.predictionDate DESC")
    Optional<MentalHealthPrediction> findLatestByUserId(@Param("userId") Long userId);

    /**
     * Find all predictions for a user
     */
    List<MentalHealthPrediction> findByUserOrderByPredictionDateDesc(User user);

    /**
     * Find predictions for a user within the date range
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.user = :user " +
            "AND p.predictionDate BETWEEN :startDate AND :endDate ORDER BY p.predictionDate DESC")
    List<MentalHealthPrediction> findByUserAndDateRange(@Param("user") User user,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find predictions by cluster category
     */
    List<MentalHealthPrediction> findByPrimaryClusterCategory(MentalHealthPrediction.ClusterCategory category);

    /**
     * Find predictions by cluster level
     */
    List<MentalHealthPrediction> findByPrimaryClusterLevel(MentalHealthPrediction.ClusterLevel level);

    /**
     * Find predictions by category and level
     */
    List<MentalHealthPrediction> findByPrimaryClusterCategoryAndPrimaryClusterLevel(
            MentalHealthPrediction.ClusterCategory category,
            MentalHealthPrediction.ClusterLevel level);

    /**
     * Find high-risk predictions (any score >= 0.8)
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE " +
            "p.stressScore >= 0.8 OR p.depressionScore >= 0.8 OR p.anxietyScore >= 0.8")
    List<MentalHealthPrediction> findHighRiskPredictions(Double threshold);

    /**
     * Find predictions by stress score range
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.stressScore >= :minScore AND p.stressScore <= :maxScore")
    List<MentalHealthPrediction> findByStressScoreRange(@Param("minScore") Double minScore,
                                                        @Param("maxScore") Double maxScore);

    /**
     * Find predictions by depression score range
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.depressionScore >= :minScore AND p.depressionScore <= :maxScore")
    List<MentalHealthPrediction> findByDepressionScoreRange(@Param("minScore") Double minScore,
                                                            @Param("maxScore") Double maxScore);

    /**
     * Find predictions by anxiety score range
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.anxietyScore >= :minScore AND p.anxietyScore <= :maxScore")
    List<MentalHealthPrediction> findByAnxietyScoreRange(@Param("minScore") Double minScore,
                                                         @Param("maxScore") Double maxScore);

    /**
     * Find predictions without cluster assignment
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.primaryClusterCategory IS NULL OR p.primaryClusterLevel IS NULL")
    List<MentalHealthPrediction> findUnclusteredPredictions();

    /**
     * Find predictions by model version
     */
    List<MentalHealthPrediction> findByModelVersion(String modelVersion);

    /**
     * Find recent predictions for clustering (Component 4 integration)
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.predictionDate >= :cutoffDate AND " +
            "(p.primaryClusterCategory IS NULL OR p.clusterAssignmentDate IS NULL)")
    List<MentalHealthPrediction> findRecentPredictionsForClustering(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get prediction statistics by category
     */
    @Query("SELECT p.primaryClusterCategory, p.primaryClusterLevel, COUNT(p), AVG(p.overallRiskScore) " +
            "FROM MentalHealthPrediction p WHERE p.primaryClusterCategory IS NOT NULL AND p.primaryClusterLevel IS NOT NULL " +
            "GROUP BY p.primaryClusterCategory, p.primaryClusterLevel")
    List<Object[]> getPredictionStatsByCluster();

    /**
     * Get daily prediction counts
     */
    @Query("SELECT DATE(p.predictionDate), COUNT(p) FROM MentalHealthPrediction p " +
            "WHERE p.predictionDate >= :startDate GROUP BY DATE(p.predictionDate) ORDER BY DATE(p.predictionDate)")
    List<Object[]> getDailyPredictionCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Find predictions for ML model retraining (Component 2)
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.user.privacyConsent = true AND p.user.dataSharingConsent = true " +
            "AND p.predictionDate >= :cutoffDate")
    List<MentalHealthPrediction> findPredictionsForMLRetraining(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get average scores by time period
     */
    @Query("SELECT DATE(p.predictionDate), AVG(p.stressScore), AVG(p.depressionScore), AVG(p.anxietyScore) " +
            "FROM MentalHealthPrediction p WHERE p.predictionDate >= :startDate " +
            "GROUP BY DATE(p.predictionDate) ORDER BY DATE(p.predictionDate)")
    List<Object[]> getAverageScoresByDate(@Param("startDate") LocalDateTime startDate);

    /**
     * Find trending predictions (score increases)
     */
    @Query("SELECT p1 FROM MentalHealthPrediction p1 WHERE EXISTS " +
            "(SELECT p2 FROM MentalHealthPrediction p2 WHERE p2.user = p1.user " +
            "AND p2.predictionDate < p1.predictionDate " +
            "AND p2.overallRiskScore < p1.overallRiskScore " +
            "AND p1.predictionDate >= :recentDate)")
    List<MentalHealthPrediction> findTrendingRiskIncreases(@Param("recentDate") LocalDateTime recentDate);

    /**
     * Get user risk progression
     */
    @Query("SELECT p.predictionDate, p.stressScore, p.depressionScore, p.anxietyScore, p.overallRiskScore " +
            "FROM MentalHealthPrediction p WHERE p.user.id = :userId ORDER BY p.predictionDate")
    List<Object[]> getUserRiskProgression(@Param("userId") Long userId);

    /**
     * Count predictions by cluster assignment
     */
    @Query("SELECT p.primaryClusterCategory, p.primaryClusterLevel, COUNT(p) " +
            "FROM MentalHealthPrediction p WHERE p.primaryClusterCategory IS NOT NULL " +
            "GROUP BY p.primaryClusterCategory, p.primaryClusterLevel")
    List<Object[]> countPredictionsByCluster();

    /**
     * Find predictions needing cluster reassignment
     */
    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.clusterAssignmentDate IS NULL OR " +
            "p.clusterAssignmentDate < :cutoffDate")
    List<MentalHealthPrediction> findPredictionsNeedingClusterReassignment(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT AVG(p.stressScore), AVG(p.depressionScore), AVG(p.anxietyScore) " +
           "FROM MentalHealthPrediction p WHERE p.primaryClusterCategory = :clusterId")
    List<Object[]> getAverageScoresByCluster(@Param("clusterId") String clusterId);

    @Query("SELECT p FROM MentalHealthPrediction p WHERE p.predictionDate >= :cutoff AND " +
           "(p.stressScore >= :threshold OR p.depressionScore >= :threshold OR p.anxietyScore >= :threshold)")
    List<MentalHealthPrediction> findRecentHighRiskPredictions(@Param("cutoff") LocalDateTime cutoff, 
                                                               @Param("threshold") Double threshold);

    List<MentalHealthPrediction> findByUserId(Long userId);


}