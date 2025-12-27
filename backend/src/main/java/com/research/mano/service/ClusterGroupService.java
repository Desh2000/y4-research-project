package com.research.mano.service;

import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.MentalHealthPrediction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cluster Group Service Interface
 * Business logic for Component 4 (GMM Clustering System)
 */
public interface ClusterGroupService extends BaseService<ClusterGroup, Long> {

    /**
     * Initialize all 9 required clusters (3x3 matrix)
     */
    void initializeAllClusters();

    /**
     * Find cluster by identifier
     */
    Optional<ClusterGroup> findByClusterIdentifier(String clusterIdentifier);

    /**
     * Find cluster by category and level
     */
    Optional<ClusterGroup> findByCategoryAndLevel(MentalHealthPrediction.ClusterCategory category,
                                                  MentalHealthPrediction.ClusterLevel level);

    /**
     * Get all cluster groups by category
     */
    List<ClusterGroup> getClustersByCategory(MentalHealthPrediction.ClusterCategory category);

    /**
     * Get all cluster groups by level
     */
    List<ClusterGroup> getClustersByLevel(MentalHealthPrediction.ClusterLevel level);

    /**
     * Find appropriate cluster for prediction scores
     */
    Optional<ClusterGroup> findClusterForPrediction(Double stressScore, Double depressionScore, Double anxietyScore);

    /**
     * Update cluster centroids (GMM model parameters)
     */
    ClusterGroup updateClusterCentroid(Long clusterId, Double stressCentroid,
                                       Double depressionCentroid, Double anxietyCentroid,
                                       String modelVersion);

    /**
     * Update cluster member count
     */
    ClusterGroup updateMemberCount(Long clusterId, Integer memberCount);

    /**
     * Increment cluster member count
     */
    void incrementMemberCount(Long clusterId);

    /**
     * Decrement cluster member count
     */
    void decrementMemberCount(Long clusterId);

    /**
     * Update average resilience score for cluster
     */
    ClusterGroup updateAverageResilienceScore(Long clusterId, Double avgScore);

    /**
     * Get cluster distribution statistics
     */
    List<Object[]> getClusterDistributionStats();

    /**
     * Get statistics by category
     */
    List<Object[]> getStatsByCategory();

    /**
     * Get statistics by level
     */
    List<Object[]> getStatsByLevel();

    /**
     * Find most populated clusters
     */
    List<ClusterGroup> getMostPopulatedClusters();

    /**
     * Find clusters with highest resilience
     */
    List<ClusterGroup> getHighestResilienceClusters();

    /**
     * Find clusters with lowest resilience
     */
    List<ClusterGroup> getLowestResilienceClusters();

    /**
     * Find non-empty clusters
     */
    List<ClusterGroup> getNonEmptyClusters();

    /**
     * Find empty clusters
     */
    List<ClusterGroup> getEmptyClusters();

    /**
     * Find clusters needing updates
     */
    List<ClusterGroup> findClustersNeedingUpdate(int daysOld, String currentModelVersion);

    /**
     * Process user assignment to cluster
     */
    void assignUserToCluster(Long userId, String clusterIdentifier);

    /**
     * Move user between clusters
     */
    void moveUserBetweenClusters(Long userId, String fromClusterIdentifier, String toClusterIdentifier);

    /**
     * Get total member count across all clusters
     */
    Long getTotalMemberCount();

    /**
     * Check if all required clusters exist
     */
    boolean areAllClustersInitialized();

    /**
     * Get missing cluster identifiers
     */
    List<String> getMissingClusterIdentifiers();

    /**
     * Update cluster with GMM parameters
     */
    ClusterGroup updateGMMParameters(Long clusterId, String covarianceMatrix, Double clusterWeight);

    /**
     * Recalculate cluster statistics
     */
    void recalculateClusterStatistics(Long clusterId);

    /**
     * Find clusters by professional support level
     */
    List<ClusterGroup> findByProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel supportLevel);

    /**
     * Get high-intensity support clusters (HIGH level)
     */
    List<ClusterGroup> getHighIntensitySupportClusters();

    /**
     * Get low-intensity support clusters (LOW level)
     */
    List<ClusterGroup> getLowIntensitySupportClusters();

    /**
     * Update cluster recommendations
     */
    ClusterGroup updateClusterRecommendations(Long clusterId, String interventions, String peerActivities);

    /**
     * Validate cluster integrity (ensure 9 clusters exist)
     */
    boolean validateClusterIntegrity();
}
