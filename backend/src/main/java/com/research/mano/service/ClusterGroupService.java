package com.research.mano.service;

import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.MentalHealthPrediction;

import java.util.List;
import java.util.Optional;

/**
 * Cluster Group Service Interface
 * Handles Component 4 (GMM Clustering System) operations
 */
public interface ClusterGroupService {

    List<ClusterGroup> findAll();

    Optional<ClusterGroup> findById(Long id);

    Optional<ClusterGroup> findByClusterIdentifier(String clusterIdentifier);

    List<ClusterGroup> getClustersByCategory(MentalHealthPrediction.ClusterCategory category);

    List<ClusterGroup> getClustersByLevel(MentalHealthPrediction.ClusterLevel level);

    Optional<ClusterGroup> findClusterForPrediction(Double stressScore, Double depressionScore, Double anxietyScore);

    List<ClusterGroup> getMostPopulatedClusters();

    List<ClusterGroup> getHighestResilienceClusters();

    List<ClusterGroup> getLowestResilienceClusters();

    List<ClusterGroup> findByProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel level);

    List<Object[]> getClusterDistributionStats();

    List<Object[]> getStatsByCategory();

    List<Object[]> getStatsByLevel();

    void initializeAllClusters();

    ClusterGroup updateClusterCentroid(Long id, Double stressCentroid, Double depressionCentroid, Double anxietyCentroid, String modelVersion);

    ClusterGroup updateClusterRecommendations(Long id, String interventions, String peerActivities);

    void recalculateClusterStatistics(Long id);

    boolean validateClusterIntegrity();

    List<String> getMissingClusterIdentifiers();

    Long getTotalMemberCount();
}