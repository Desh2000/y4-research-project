package com.research.mano.repository;

import com.research.mano.entity.ClusterTransition;
import com.research.mano.entity.ClusterTransition.TransitionType;
import com.research.mano.entity.ClusterTransition.TransitionDirection;
import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cluster Transition Repository for Component 4
 */
@Repository
public interface ClusterTransitionRepository extends BaseRepository<ClusterTransition, Long> {

    // ==================== USER QUERIES ====================

    List<ClusterTransition> findByUser(User user);

    List<ClusterTransition> findByUserOrderByTransitionDateDesc(User user);

    Page<ClusterTransition> findByUserOrderByTransitionDateDesc(User user, Pageable pageable);

    @Query("SELECT t FROM ClusterTransition t WHERE t.user.id = :userId ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findByUserIdOrdered(@Param("userId") Long userId);

    @Query("SELECT t FROM ClusterTransition t WHERE t.user.id = :userId ORDER BY t.transitionDate DESC LIMIT 1")
    Optional<ClusterTransition> findLatestByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM ClusterTransition t WHERE t.user.id = :userId AND t.transitionType = 'INITIAL_ASSIGNMENT'")
    Optional<ClusterTransition> findInitialAssignmentByUserId(@Param("userId") Long userId);

    // ==================== CLUSTER QUERIES ====================

    List<ClusterTransition> findByFromCluster(ClusterGroup cluster);

    List<ClusterTransition> findByToCluster(ClusterGroup cluster);

    @Query("SELECT t FROM ClusterTransition t WHERE t.fromCluster.id = :clusterId OR t.toCluster.id = :clusterId ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findByClusterId(@Param("clusterId") Long clusterId);

    @Query("SELECT t FROM ClusterTransition t WHERE t.fromCluster.id = :fromId AND t.toCluster.id = :toId")
    List<ClusterTransition> findByFromAndToCluster(@Param("fromId") Long fromId, @Param("toId") Long toId);

    // ==================== TYPE QUERIES ====================

    List<ClusterTransition> findByTransitionType(TransitionType type);

    List<ClusterTransition> findByTransitionDirection(TransitionDirection direction);

    @Query("SELECT t FROM ClusterTransition t WHERE t.transitionType = 'IMPROVEMENT' ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findImprovements();

    @Query("SELECT t FROM ClusterTransition t WHERE t.transitionType = 'DETERIORATION' ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findDeteriorations();

    // ==================== DATE RANGE QUERIES ====================

    @Query("SELECT t FROM ClusterTransition t WHERE t.transitionDate BETWEEN :start AND :end ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM ClusterTransition t WHERE t.user.id = :userId AND t.transitionDate BETWEEN :start AND :end ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("SELECT t FROM ClusterTransition t WHERE t.transitionDate >= :since ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findRecentTransitions(@Param("since") LocalDateTime since);

    // ==================== OUTCOME QUERIES ====================

    @Query("SELECT t FROM ClusterTransition t WHERE t.outcomeTracked = false AND t.transitionDate < :date")
    List<ClusterTransition> findTransitionsNeedingOutcomeTracking(@Param("date") LocalDateTime date);

    @Query("SELECT t FROM ClusterTransition t WHERE t.transitionSuccessful = true ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findSuccessfulTransitions();

    @Query("SELECT t FROM ClusterTransition t WHERE t.transitionSuccessful = false ORDER BY t.transitionDate DESC")
    List<ClusterTransition> findUnsuccessfulTransitions();

    // ==================== TRIGGER QUERIES ====================

    @Query("SELECT t FROM ClusterTransition t WHERE t.triggerPredictionId = :predictionId")
    Optional<ClusterTransition> findByTriggerPredictionId(@Param("predictionId") Long predictionId);

    @Query("SELECT t FROM ClusterTransition t WHERE t.triggerInterventionId = :interventionId")
    Optional<ClusterTransition> findByTriggerInterventionId(@Param("interventionId") Long interventionId);

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT COUNT(t) FROM ClusterTransition t WHERE t.transitionDate >= :since")
    Long countTransitionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT t.transitionType, COUNT(t) FROM ClusterTransition t WHERE t.transitionDate >= :since GROUP BY t.transitionType")
    List<Object[]> getTransitionTypeDistribution(@Param("since") LocalDateTime since);

    @Query("SELECT t.transitionDirection, COUNT(t) FROM ClusterTransition t WHERE t.transitionDate >= :since GROUP BY t.transitionDirection")
    List<Object[]> getTransitionDirectionDistribution(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(t.severityChange) FROM ClusterTransition t WHERE t.transitionDate >= :since AND t.severityChange IS NOT NULL")
    Double getAverageSeverityChange(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(t.daysInPreviousCluster) FROM ClusterTransition t WHERE t.daysInPreviousCluster IS NOT NULL")
    Double getAverageDaysInCluster();

    // ==================== TRANSITION MATRIX ====================

    @Query("SELECT t.fromCluster.clusterIdentifier, t.toCluster.clusterIdentifier, COUNT(t) " +
            "FROM ClusterTransition t WHERE t.fromCluster IS NOT NULL AND t.transitionDate >= :since " +
            "GROUP BY t.fromCluster.clusterIdentifier, t.toCluster.clusterIdentifier")
    List<Object[]> getTransitionMatrix(@Param("since") LocalDateTime since);

    @Query("SELECT t.fromCluster.primaryCategory, t.toCluster.primaryCategory, COUNT(t) " +
            "FROM ClusterTransition t WHERE t.fromCluster IS NOT NULL AND t.transitionDate >= :since " +
            "GROUP BY t.fromCluster.primaryCategory, t.toCluster.primaryCategory")
    List<Object[]> getCategoryTransitionMatrix(@Param("since") LocalDateTime since);

    @Query("SELECT t.fromCluster.severityLevel, t.toCluster.severityLevel, COUNT(t) " +
            "FROM ClusterTransition t WHERE t.fromCluster IS NOT NULL AND t.transitionDate >= :since " +
            "GROUP BY t.fromCluster.severityLevel, t.toCluster.severityLevel")
    List<Object[]> getSeverityTransitionMatrix(@Param("since") LocalDateTime since);

    // ==================== USER JOURNEY ====================

    @Query("SELECT COUNT(DISTINCT t.user) FROM ClusterTransition t WHERE t.transitionType = 'IMPROVEMENT' AND t.transitionDate >= :since")
    Long countUsersWithImprovement(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT t.user) FROM ClusterTransition t WHERE t.transitionType = 'DETERIORATION' AND t.transitionDate >= :since")
    Long countUsersWithDeterioration(@Param("since") LocalDateTime since);

    @Query("SELECT t.user.id, COUNT(t) FROM ClusterTransition t GROUP BY t.user.id HAVING COUNT(t) > :minTransitions")
    List<Object[]> findUsersWithManyTransitions(@Param("minTransitions") Long minTransitions);
}