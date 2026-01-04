package com.research.mano.service;

import com.research.mano.dto.cluster.*;
import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.ClusterGroup.ClusterCategory;
import com.research.mano.entity.ClusterGroup.SeverityLevel;
import com.research.mano.entity.ClusterTransition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Clustering Service Interface for Component 4
 * Handles GMM-based clustering and community support features
 */
public interface ClusteringService {

    // ==================== CLUSTER ASSIGNMENT ====================

    /**
     * Assign user to appropriate cluster based on scores
     */
    ClusterAssignmentResult assignUserToCluster(Long userId, ClusterAssignmentRequest request);

    /**
     * Get cluster assignment without persisting (preview)
     */
    ClusterAssignmentResult previewClusterAssignment(Double stressScore, Double depressionScore, Double anxietyScore);

    /**
     * Reassess and potentially reassign user's cluster
     */
    ClusterAssignmentResult reassessUserCluster(Long userId);

    /**
     * Get user's current cluster
     */
    Optional<ClusterGroupDTO> getUserCurrentCluster(Long userId);

    /**
     * Find nearest cluster for given scores
     */
    ClusterGroupDTO findNearestCluster(Double stressScore, Double depressionScore, Double anxietyScore);

    /**
     * Get top N nearest clusters with distances
     */
    List<ClusterAssignmentResult.AlternativeCluster> findNearestClusters(
            Double stressScore, Double depressionScore, Double anxietyScore, int topN);

    // ==================== CLUSTER MANAGEMENT ====================

    /**
     * Get all active clusters
     */
    List<ClusterGroupDTO> getAllActiveClusters();

    /**
     * Get cluster by ID
     */
    Optional<ClusterGroupDTO> getClusterById(Long clusterId);

    /**
     * Get cluster by identifier
     */
    Optional<ClusterGroupDTO> getClusterByIdentifier(String identifier);

    /**
     * Get clusters by category
     */
    List<ClusterGroupDTO> getClustersByCategory(ClusterCategory category);

    /**
     * Get clusters by severity level
     */
    List<ClusterGroupDTO> getClustersBySeverity(SeverityLevel severityLevel);

    /**
     * Create new cluster
     */
    ClusterGroupDTO createCluster(ClusterGroupDTO clusterDTO);

    /**
     * Update cluster
     */
    ClusterGroupDTO updateCluster(Long clusterId, ClusterGroupDTO clusterDTO);

    /**
     * Deactivate cluster
     */
    void deactivateCluster(Long clusterId);

    /**
     * Update cluster centroids from ML service
     */
    ClusterGroupDTO updateClusterCentroids(Long clusterId, Double stressCentroid,
                                           Double depressionCentroid, Double anxietyCentroid);

    // ==================== TRANSITION MANAGEMENT ====================

    /**
     * Get user's cluster transition history
     */
    List<ClusterTransitionDTO> getUserTransitionHistory(Long userId);

    /**
     * Get user's transition history paginated
     */
    List<ClusterTransitionDTO> getUserTransitionHistory(Long userId, int page, int size);

    /**
     * Get user's latest transition
     */
    Optional<ClusterTransitionDTO> getUserLatestTransition(Long userId);

    /**
     * Get transitions for a cluster
     */
    List<ClusterTransitionDTO> getClusterTransitions(Long clusterId);

    /**
     * Get recent transitions system-wide
     */
    List<ClusterTransitionDTO> getRecentTransitions(int days);

    /**
     * Get improvements (positive transitions)
     */
    List<ClusterTransitionDTO> getImprovements(int days);

    /**
     * Get deteriorations (negative transitions)
     */
    List<ClusterTransitionDTO> getDeteriorations(int days);

    /**
     * Track outcome of a transition
     */
    ClusterTransitionDTO trackTransitionOutcome(Long transitionId, boolean successful, String notes);

    // ==================== ANALYTICS ====================

    /**
     * Get cluster statistics
     */
    Map<String, Object> getClusterStatistics();

    /**
     * Get statistics for a specific cluster
     */
    Map<String, Object> getClusterDetailedStatistics(Long clusterId);

    /**
     * Get transition matrix
     */
    Map<String, Object> getTransitionMatrix(int days);

    /**
     * Get category transition matrix
     */
    Map<String, Object> getCategoryTransitionMatrix(int days);

    /**
     * Get severity transition matrix
     */
    Map<String, Object> getSeverityTransitionMatrix(int days);

    /**
     * Get user journey analysis
     */
    Map<String, Object> getUserJourneyAnalysis(Long userId);

    /**
     * Get cluster performance metrics
     */
    Map<String, Object> getClusterPerformanceMetrics();

    /**
     * Get high-severity cluster statistics
     */
    Map<String, Object> getHighSeverityStatistics();

    // ==================== COMMUNITY FEATURES ====================

    /**
     * Get cluster members (anonymized)
     */
    Map<String, Object> getClusterMemberStatistics(Long clusterId);

    /**
     * Get clusters with peer support
     */
    List<ClusterGroupDTO> getClustersWithPeerSupport();

    /**
     * Get recommended interventions for cluster
     */
    List<String> getClusterRecommendedInterventions(Long clusterId);

    /**
     * Get coping strategies for cluster
     */
    List<String> getClusterCopingStrategies(Long clusterId);

    /**
     * Get shared experiences for cluster
     */
    List<String> getClusterSharedExperiences(Long clusterId);

    // ==================== MODEL MANAGEMENT ====================

    /**
     * Trigger cluster model update from ML service
     */
    boolean triggerModelUpdate();

    /**
     * Get available model versions
     */
    List<String> getAvailableModelVersions();

    /**
     * Update clusters from ML service response
     */
    List<ClusterGroupDTO> updateClustersFromMLService(Map<String, Object> mlResponse);

    /**
     * Initialize default clusters
     */
    List<ClusterGroupDTO> initializeDefaultClusters();

    /**
     * Check ML clustering service health
     */
    boolean isClusteringServiceHealthy();

    // ==================== ALERTS AND MONITORING ====================

    /**
     * Get clusters requiring attention
     */
    List<ClusterGroupDTO> getClustersRequiringAttention();

    /**
     * Get critical clusters (severe with active members)
     */
    List<ClusterGroupDTO> getCriticalClusters();

    /**
     * Get clusters needing review
     */
    List<ClusterGroupDTO> getClustersNeedingReview();

    /**
     * Mark cluster as reviewed
     */
    ClusterGroupDTO markClusterAsReviewed(Long clusterId, String reviewedBy);
}