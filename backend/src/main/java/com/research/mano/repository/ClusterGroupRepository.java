package com.research.mano.repository;



import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.MentalHealthPrediction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Cluster Group Repository Interface
 * Handles CRUD operations for Component 4 (GMM Clustering) groups
 */
@Repository
public interface ClusterGroupRepository extends BaseRepository<ClusterGroup, Long> {

    /**
     * Find cluster group by identifier
     */
    Optional<ClusterGroup> findByClusterIdentifier(String clusterIdentifier);

    /**
     * Find cluster groups by category
     */
    List<ClusterGroup> findByCategory(MentalHealthPrediction.ClusterCategory category);

    /**
     * Find cluster groups by level
     */
    List<ClusterGroup> findByLevel(MentalHealthPrediction.ClusterLevel level);

    /**
     * Find cluster group by category and level
     */
    Optional<ClusterGroup> findByCategoryAndLevel(MentalHealthPrediction.ClusterCategory category,
                                                  MentalHealthPrediction.ClusterLevel level);

    /**
     * Find all active cluster groups
     */
    List<ClusterGroup> findByIsActiveTrue();

    /**
     * Find non-empty cluster groups (with members)
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE cg.memberCount > 0")
    List<ClusterGroup> findNonEmptyClusterGroups();

    /**
     * Find empty cluster groups (no members)
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE cg.memberCount = 0 OR cg.memberCount IS NULL")
    List<ClusterGroup> findEmptyClusterGroups();

    /**
     * Find cluster groups by professional support level
     */
    List<ClusterGroup> findByProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel supportLevel);

    /**
     * Find high-intensity support clusters (HIGH level clusters)
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE cg.level = 'HIGH'")
    List<ClusterGroup> findHighIntensitySupportClusters();

    /**
     * Find low-intensity support clusters (LOW level clusters)
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE cg.level = 'LOW'")
    List<ClusterGroup> findLowIntensitySupportClusters();

    /**
     * Find cluster groups by model version
     */
    List<ClusterGroup> findByModelVersion(String modelVersion);

    /**
     * Find cluster groups needing updates (old model version or old timestamp)
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE cg.lastUpdated < :cutoffDate OR cg.modelVersion != :currentVersion")
    List<ClusterGroup> findClusterGroupsNeedingUpdate(@Param("cutoffDate") LocalDateTime cutoffDate,
                                                      @Param("currentVersion") String currentVersion);

    /**
     * Get cluster distribution statistics
     */
    @Query("SELECT cg.clusterIdentifier, cg.memberCount, cg.averageResilienceScore " +
            "FROM ClusterGroup cg ORDER BY cg.memberCount DESC")
    List<Object[]> getClusterDistributionStats();

    /**
     * Get cluster statistics by category
     */
    @Query("SELECT cg.category, SUM(cg.memberCount), AVG(cg.averageResilienceScore) " +
            "FROM ClusterGroup cg GROUP BY cg.category")
    List<Object[]> getStatsByCategory();

    /**
     * Get cluster statistics by level
     */
    @Query("SELECT cg.level, SUM(cg.memberCount), AVG(cg.averageResilienceScore) " +
            "FROM ClusterGroup cg GROUP BY cg.level")
    List<Object[]> getStatsByLevel();

    /**
     * Find the most populated clusters
     */
    @Query("SELECT cg FROM ClusterGroup cg ORDER BY cg.memberCount DESC")
    List<ClusterGroup> findMostPopulatedClusters();

    /**
     * Find clusters with the highest average resilience
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE cg.averageResilienceScore IS NOT NULL " +
            "ORDER BY cg.averageResilienceScore DESC")
    List<ClusterGroup> findHighestResilienceClusters();

    /**
     * Find clusters with the lowest average resilience
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE cg.averageResilienceScore IS NOT NULL " +
            "ORDER BY cg.averageResilienceScore ASC")
    List<ClusterGroup> findLowestResilienceClusters();

    /**
     * Update cluster member count
     */
    @Modifying
    @Query("UPDATE ClusterGroup cg SET cg.memberCount = :memberCount, cg.lastUpdated = :updateTime " +
            "WHERE cg.id = :clusterId")
    void updateMemberCount(@Param("clusterId") Long clusterId,
                           @Param("memberCount") Integer memberCount,
                           @Param("updateTime") LocalDateTime updateTime);

    /**
     * Update cluster centroid values (GMM model parameters)
     */
    @Modifying
    @Query("UPDATE ClusterGroup cg SET " +
            "cg.centroidStress = :stressCentroid, " +
            "cg.centroidDepression = :depressionCentroid, " +
            "cg.centroidAnxiety = :anxietyCentroid, " +
            "cg.lastUpdated = :updateTime, " +
            "cg.modelVersion = :modelVersion " +
            "WHERE cg.id = :clusterId")
    void updateCentroid(@Param("clusterId") Long clusterId,
                        @Param("stressCentroid") Double stressCentroid,
                        @Param("depressionCentroid") Double depressionCentroid,
                        @Param("anxietyCentroid") Double anxietyCentroid,
                        @Param("updateTime") LocalDateTime updateTime,
                        @Param("modelVersion") String modelVersion);

    /**
     * Update cluster average resilience score
     */
    @Modifying
    @Query("UPDATE ClusterGroup cg SET cg.averageResilienceScore = :avgScore, cg.lastUpdated = :updateTime " +
            "WHERE cg.id = :clusterId")
    void updateAverageResilienceScore(@Param("clusterId") Long clusterId,
                                      @Param("avgScore") Double avgScore,
                                      @Param("updateTime") LocalDateTime updateTime);

    /**
     * Increment member count for a cluster
     */
    @Modifying
    @Query("UPDATE ClusterGroup cg SET " +
            "cg.memberCount = COALESCE(cg.memberCount, 0) + 1, " +
            "cg.lastUpdated = :updateTime " +
            "WHERE cg.id = :clusterId")
    void incrementMemberCount(@Param("clusterId") Long clusterId, @Param("updateTime") LocalDateTime updateTime);

    /**
     * Decrement member count for a cluster
     */
    @Modifying
    @Query("UPDATE ClusterGroup cg SET " +
            "cg.memberCount = CASE WHEN COALESCE(cg.memberCount, 0) > 0 THEN cg.memberCount - 1 ELSE 0 END, " +
            "cg.lastUpdated = :updateTime " +
            "WHERE cg.id = :clusterId")
    void decrementMemberCount(@Param("clusterId") Long clusterId, @Param("updateTime") LocalDateTime updateTime);

    /**
     * Find clusters for specific score ranges (for Component 2 integration)
     */
    @Query("SELECT cg FROM ClusterGroup cg WHERE " +
            "(cg.category = 'STRESS' AND :stressScore BETWEEN " +
            "   (CASE cg.level WHEN 'LOW' THEN 0.1 WHEN 'MEDIUM' THEN 0.4 WHEN 'HIGH' THEN 0.8 END) AND " +
            "   (CASE cg.level WHEN 'LOW' THEN 0.3 WHEN 'MEDIUM' THEN 0.7 WHEN 'HIGH' THEN 1.0 END)" +
            ") OR " +
            "(cg.category = 'DEPRESSION' AND :depressionScore BETWEEN " +
            "   (CASE cg.level WHEN 'LOW' THEN 0.1 WHEN 'MEDIUM' THEN 0.4 WHEN 'HIGH' THEN 0.8 END) AND " +
            "   (CASE cg.level WHEN 'LOW' THEN 0.3 WHEN 'MEDIUM' THEN 0.7 WHEN 'HIGH' THEN 1.0 END)" +
            ") OR " +
            "(cg.category = 'ANXIETY' AND :anxietyScore BETWEEN " +
            "   (CASE cg.level WHEN 'LOW' THEN 0.1 WHEN 'MEDIUM' THEN 0.4 WHEN 'HIGH' THEN 0.8 END) AND " +
            "   (CASE cg.level WHEN 'LOW' THEN 0.3 WHEN 'MEDIUM' THEN 0.7 WHEN 'HIGH' THEN 1.0 END)" +
            ")")
    List<ClusterGroup> findClustersForScores(@Param("stressScore") Double stressScore,
                                             @Param("depressionScore") Double depressionScore,
                                             @Param("anxietyScore") Double anxietyScore);

    /**
     * Get the total member count across all clusters
     */
    @Query("SELECT SUM(cg.memberCount) FROM ClusterGroup cg")
    Long getTotalMemberCount();

    /**
     * Check if all 9 required clusters exist
     */
    @Query("SELECT COUNT(cg) FROM ClusterGroup cg WHERE cg.isActive = true")
    Long countActiveClusters();

    /**
     * Find all existing cluster identifiers
     */
    @Query("SELECT cg.clusterIdentifier FROM ClusterGroup cg")
    List<String> findAllClusterIdentifiers();

    /**
     * Find clusters that need initialization (missing required clusters)
     */
    default List<String> findMissingClusterIdentifiers() {
        List<String> existingIdentifiers = findAllClusterIdentifiers();
        List<String> allPossibleIdentifiers = new java.util.ArrayList<>();
        for (MentalHealthPrediction.ClusterCategory category : MentalHealthPrediction.ClusterCategory.values()) {
            for (MentalHealthPrediction.ClusterLevel level : MentalHealthPrediction.ClusterLevel.values()) {
                allPossibleIdentifiers.add(category.name() + "_" + level.name());
            }
        }
        allPossibleIdentifiers.removeAll(existingIdentifiers);
        return allPossibleIdentifiers;
    }
}