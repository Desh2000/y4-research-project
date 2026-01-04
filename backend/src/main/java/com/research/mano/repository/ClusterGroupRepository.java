package com.research.mano.repository;

import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.ClusterGroup.ClusterCategory;
import com.research.mano.entity.ClusterGroup.SeverityLevel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cluster Group Repository for Component 4
 */
@Repository
public interface ClusterGroupRepository extends BaseRepository<ClusterGroup, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<ClusterGroup> findByClusterIdentifier(String clusterIdentifier);

    List<ClusterGroup> findByIsActiveTrue();

    List<ClusterGroup> findByPrimaryCategory(ClusterCategory category);

    List<ClusterGroup> findBySeverityLevel(SeverityLevel severityLevel);

    List<ClusterGroup> findByPrimaryCategoryAndSeverityLevel(ClusterCategory category, SeverityLevel severityLevel);

    // ==================== ACTIVE CLUSTER QUERIES ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true ORDER BY c.primaryCategory, c.severityLevel")
    List<ClusterGroup> findAllActiveOrdered();

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.primaryCategory = :category ORDER BY c.severityLevel")
    List<ClusterGroup> findActiveByCategoryOrdered(@Param("category") ClusterCategory category);

    // ==================== MEMBER STATISTICS ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.memberCount > 0 ORDER BY c.memberCount DESC")
    List<ClusterGroup> findClustersWithMembers();

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.activeMemberCount > :minMembers")
    List<ClusterGroup> findClustersWithMinActiveMembers(@Param("minMembers") Integer minMembers);

    @Query("SELECT SUM(c.memberCount) FROM ClusterGroup c WHERE c.isActive = true")
    Long getTotalMemberCount();

    @Query("SELECT SUM(c.activeMemberCount) FROM ClusterGroup c WHERE c.isActive = true")
    Long getTotalActiveMemberCount();

    // ==================== SEVERITY QUERIES ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.severityLevel IN ('HIGH', 'SEVERE') ORDER BY c.activeMemberCount DESC")
    List<ClusterGroup> findHighSeverityClusters();

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.severityLevel = 'SEVERE' AND c.activeMemberCount > 0")
    List<ClusterGroup> findCriticalClusters();

    // ==================== PERFORMANCE QUERIES ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.avgImprovementRate > :threshold ORDER BY c.avgImprovementRate DESC")
    List<ClusterGroup> findHighPerformingClusters(@Param("threshold") Double threshold);

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.avgImprovementRate < :threshold ORDER BY c.avgImprovementRate ASC")
    List<ClusterGroup> findLowPerformingClusters(@Param("threshold") Double threshold);

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true ORDER BY c.successfulTransitions DESC")
    List<ClusterGroup> findBySuccessfulTransitionsDesc();

    // ==================== MODEL QUERIES ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.modelVersion = :version AND c.isActive = true")
    List<ClusterGroup> findByModelVersion(@Param("version") String version);

    @Query("SELECT DISTINCT c.modelVersion FROM ClusterGroup c WHERE c.modelVersion IS NOT NULL ORDER BY c.lastModelUpdate DESC")
    List<String> findDistinctModelVersions();

    @Query("SELECT c FROM ClusterGroup c WHERE c.lastModelUpdate < :date")
    List<ClusterGroup> findClustersNeedingUpdate(@Param("date") LocalDateTime date);

    // ==================== CENTROID QUERIES ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.centroidStress IS NOT NULL ORDER BY c.centroidStress DESC")
    List<ClusterGroup> findOrderedByStressCentroid();

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND " +
            "ABS(c.centroidStress - :stress) + ABS(c.centroidDepression - :depression) + ABS(c.centroidAnxiety - :anxiety) = " +
            "(SELECT MIN(ABS(c2.centroidStress - :stress) + ABS(c2.centroidDepression - :depression) + ABS(c2.centroidAnxiety - :anxiety)) " +
            "FROM ClusterGroup c2 WHERE c2.isActive = true AND c2.centroidStress IS NOT NULL)")
    Optional<ClusterGroup> findNearestCluster(@Param("stress") Double stress,
                                              @Param("depression") Double depression,
                                              @Param("anxiety") Double anxiety);

    // ==================== COMMUNITY QUERIES ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.hasPeerSupport = true")
    List<ClusterGroup> findClustersWithPeerSupport();

    @Query("SELECT c FROM ClusterGroup c WHERE c.isActive = true AND c.communityEngagementScore > :threshold")
    List<ClusterGroup> findHighEngagementClusters(@Param("threshold") Double threshold);

    // ==================== REVIEW QUERIES ====================

    @Query("SELECT c FROM ClusterGroup c WHERE c.requiresReview = true")
    List<ClusterGroup> findClustersRequiringReview();

    @Query("SELECT c FROM ClusterGroup c WHERE c.lastReviewed < :date OR c.lastReviewed IS NULL")
    List<ClusterGroup> findClustersNeedingReview(@Param("date") LocalDateTime date);

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT c.primaryCategory, COUNT(c), SUM(c.memberCount) FROM ClusterGroup c WHERE c.isActive = true GROUP BY c.primaryCategory")
    List<Object[]> getStatisticsByCategory();

    @Query("SELECT c.severityLevel, COUNT(c), SUM(c.memberCount) FROM ClusterGroup c WHERE c.isActive = true GROUP BY c.severityLevel")
    List<Object[]> getStatisticsBySeverity();

    @Query("SELECT AVG(c.avgImprovementRate), AVG(c.retentionRate), AVG(c.silhouetteScore) FROM ClusterGroup c WHERE c.isActive = true")
    Object[] getOverallPerformanceMetrics();

    @Query("SELECT c.primaryCategory, AVG(c.avgImprovementRate) FROM ClusterGroup c WHERE c.isActive = true GROUP BY c.primaryCategory")
    List<Object[]> getImprovementRateByCategory();

    // ==================== UPDATE QUERIES ====================

    @Modifying
    @Query("UPDATE ClusterGroup c SET c.memberCount = c.memberCount + 1, c.activeMemberCount = c.activeMemberCount + 1 WHERE c.id = :clusterId")
    int incrementMemberCount(@Param("clusterId") Long clusterId, LocalDateTime now);

    @Modifying
    @Query("UPDATE ClusterGroup c SET c.activeMemberCount = c.activeMemberCount - 1 WHERE c.id = :clusterId AND c.activeMemberCount > 0")
    int decrementActiveMemberCount(@Param("clusterId") Long clusterId);

    @Modifying
    @Query("UPDATE ClusterGroup c SET c.successfulTransitions = c.successfulTransitions + 1 WHERE c.id = :clusterId")
    int incrementSuccessfulTransitions(@Param("clusterId") Long clusterId);

    @Modifying
    @Query("UPDATE ClusterGroup c SET c.isActive = false WHERE c.memberCount = 0 AND c.lastModelUpdate < :date")
    int deactivateEmptyClusters(@Param("date") LocalDateTime date);

    @Modifying
    @Query("UPDATE ClusterGroup c SET c.memberCount = c.memberCount - 1 WHERE c.id = :clusterId AND c.memberCount > 0")
    void decrementMemberCount(@Param("clusterId") Long clusterId, @Param("now") LocalDateTime now);

    @Query("SELECT c FROM ClusterGroup c WHERE c.primaryCategory = :category")
    List<ClusterGroup> findByCategory(@Param("category") ClusterCategory category);

    @Query("SELECT c FROM ClusterGroup c WHERE c.severityLevel = :level")
    List<ClusterGroup> findByLevel(@Param("level") SeverityLevel level);

    @Query("SELECT c FROM ClusterGroup c WHERE c.primaryCategory = :category AND c.severityLevel = :level")
    Optional<ClusterGroup> findByCategoryAndLevel(@Param("category") ClusterCategory category, @Param("level") SeverityLevel level);

    List<ClusterGroup> findTop5ByOrderByMemberCountDesc();

    List<ClusterGroup> findTop5ByOrderByAverageResilienceScoreDesc();

    List<ClusterGroup> findTop5ByOrderByAverageResilienceScoreAsc();

    List<ClusterGroup> findByProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel level);

    @Query("SELECT c.primaryCategory, COUNT(c) FROM ClusterGroup c GROUP BY c.primaryCategory")
    List<Object[]> getClusterDistributionStats();

    @Query("SELECT c.primaryCategory, AVG(c.memberCount), MAX(c.memberCount), MIN(c.memberCount) FROM ClusterGroup c GROUP BY c.primaryCategory")
    List<Object[]> getStatsByCategory();

    @Query("SELECT c.severityLevel, AVG(c.memberCount), MAX(c.memberCount), MIN(c.memberCount) FROM ClusterGroup c GROUP BY c.severityLevel")
    List<Object[]> getStatsByLevel();

    @Query("SELECT SUM(c.memberCount) FROM ClusterGroup c")
    Long sumMemberCount();
}