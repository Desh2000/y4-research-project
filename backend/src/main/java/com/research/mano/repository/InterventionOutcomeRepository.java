package com.research.mano.repository;

import com.research.mano.entity.InterventionOutcome;
import com.research.mano.entity.InterventionOutcome.OutcomeType;
import com.research.mano.entity.InterventionOutcome.OutcomeStatus;
import com.research.mano.entity.InterventionOutcome.EffectivenessRating;
import com.research.mano.entity.InterventionOutcome.ResponseType;
import com.research.mano.entity.Intervention;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Intervention Outcome Repository for Component 1
 * Provides data access for intervention results (real and simulated)
 */
@Repository
public interface InterventionOutcomeRepository extends BaseRepository<InterventionOutcome, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<InterventionOutcome> findByOutcomeCode(String outcomeCode);

    boolean existsByOutcomeCode(String outcomeCode);

    List<InterventionOutcome> findByUser(User user);

    List<InterventionOutcome> findByUserId(Long userId);

    List<InterventionOutcome> findByIntervention(Intervention intervention);

    List<InterventionOutcome> findByInterventionId(Long interventionId);

    // ==================== STATUS AND TYPE QUERIES ====================

    List<InterventionOutcome> findByStatus(OutcomeStatus status);

    List<InterventionOutcome> findByOutcomeType(OutcomeType outcomeType);

    List<InterventionOutcome> findByUserAndStatus(User user, OutcomeStatus status);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.user.id = :userId AND o.status = :status")
    List<InterventionOutcome> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") OutcomeStatus status);

    // ==================== SIMULATION QUERIES (Component 1) ====================

    List<InterventionOutcome> findByIsSimulatedTrue();

    List<InterventionOutcome> findByIsSimulatedFalse();

    @Query("SELECT o FROM InterventionOutcome o WHERE o.isSimulated = true AND o.simulationModelVersion = :version")
    List<InterventionOutcome> findBySimulationVersion(@Param("version") String version);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.syntheticDataRecordId = :recordId")
    List<InterventionOutcome> findBySyntheticDataRecordId(@Param("recordId") Long recordId);

    @Query("SELECT COUNT(o) FROM InterventionOutcome o WHERE o.isSimulated = true")
    long countSimulatedOutcomes();

    @Query("SELECT COUNT(o) FROM InterventionOutcome o WHERE o.isSimulated = false")
    long countRealOutcomes();

    // ==================== EFFECTIVENESS QUERIES ====================

    List<InterventionOutcome> findByEffectivenessRating(EffectivenessRating rating);

    List<InterventionOutcome> findByResponseType(ResponseType responseType);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.overallImprovementScore >= :minImprovement")
    List<InterventionOutcome> findByMinImprovement(@Param("minImprovement") Double minImprovement);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.metExpectedOutcome = true")
    List<InterventionOutcome> findSuccessfulOutcomes();

    @Query("SELECT o FROM InterventionOutcome o WHERE o.metExpectedOutcome = false AND o.status = 'COMPLETED'")
    List<InterventionOutcome> findUnsuccessfulOutcomes();

    // ==================== USER OUTCOME QUERIES ====================

    @Query("SELECT o FROM InterventionOutcome o WHERE o.user.id = :userId ORDER BY o.interventionStartDate DESC")
    List<InterventionOutcome> findUserOutcomesChronologically(@Param("userId") Long userId);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.user.id = :userId AND o.status = 'COMPLETED' " +
            "ORDER BY o.overallImprovementScore DESC")
    List<InterventionOutcome> findUserBestOutcomes(@Param("userId") Long userId);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.user.id = :userId AND o.status = 'IN_PROGRESS'")
    List<InterventionOutcome> findUserActiveInterventions(@Param("userId") Long userId);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.user = :user ORDER BY o.interventionStartDate DESC LIMIT 1")
    Optional<InterventionOutcome> findLatestByUser(@Param("user") User user);

    // ==================== INTERVENTION EFFECTIVENESS ANALYSIS ====================

    @Query("SELECT AVG(o.overallImprovementScore) FROM InterventionOutcome o " +
            "WHERE o.intervention.id = :interventionId AND o.status = 'COMPLETED'")
    Double getAverageImprovementForIntervention(@Param("interventionId") Long interventionId);

    @Query("SELECT AVG(o.overallImprovementScore) FROM InterventionOutcome o " +
            "WHERE o.intervention.interventionType = :type AND o.status = 'COMPLETED'")
    Double getAverageImprovementByType(@Param("type") InterventionType type);

    @Query("SELECT o.intervention.interventionType, AVG(o.overallImprovementScore), COUNT(o) " +
            "FROM InterventionOutcome o WHERE o.status = 'COMPLETED' " +
            "GROUP BY o.intervention.interventionType")
    List<Object[]> getEffectivenessByInterventionType();

    @Query("SELECT o.effectivenessRating, COUNT(o) FROM InterventionOutcome o " +
            "WHERE o.intervention.id = :interventionId GROUP BY o.effectivenessRating")
    List<Object[]> getEffectivenessDistribution(@Param("interventionId") Long interventionId);

    // ==================== ADHERENCE QUERIES ====================

    @Query("SELECT o FROM InterventionOutcome o WHERE o.adherencePercentage >= :minAdherence AND o.status = 'COMPLETED'")
    List<InterventionOutcome> findHighAdherenceOutcomes(@Param("minAdherence") Double minAdherence);

    List<InterventionOutcome> findByDropoutTrue();

    @Query("SELECT AVG(o.adherencePercentage) FROM InterventionOutcome o WHERE o.intervention.id = :interventionId")
    Double getAverageAdherenceForIntervention(@Param("interventionId") Long interventionId);

    @Query("SELECT o.dropoutWeek, COUNT(o) FROM InterventionOutcome o " +
            "WHERE o.dropout = true AND o.intervention.id = :interventionId GROUP BY o.dropoutWeek")
    List<Object[]> getDropoutDistributionByWeek(@Param("interventionId") Long interventionId);

    // ==================== DATE RANGE QUERIES ====================

    @Query("SELECT o FROM InterventionOutcome o WHERE o.interventionStartDate BETWEEN :start AND :end")
    List<InterventionOutcome> findByDateRange(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.user.id = :userId " +
            "AND o.interventionStartDate BETWEEN :start AND :end")
    List<InterventionOutcome> findUserOutcomesByDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ==================== CLUSTER TRANSITION QUERIES ====================

    @Query("SELECT o FROM InterventionOutcome o WHERE o.clusterTransitionOccurred = true")
    List<InterventionOutcome> findWithClusterTransitions();

    @Query("SELECT o.preClusterIdentifier, o.postClusterIdentifier, COUNT(o) " +
            "FROM InterventionOutcome o WHERE o.clusterTransitionOccurred = true " +
            "GROUP BY o.preClusterIdentifier, o.postClusterIdentifier")
    List<Object[]> getClusterTransitionMatrix();

    @Query("SELECT o FROM InterventionOutcome o WHERE o.preClusterIdentifier = :clusterId")
    List<InterventionOutcome> findByPreCluster(@Param("clusterId") String clusterId);

    // ==================== SCORE IMPROVEMENT QUERIES ====================

    @Query("SELECT o FROM InterventionOutcome o WHERE o.stressChange < :threshold AND o.status = 'COMPLETED'")
    List<InterventionOutcome> findWithStressImprovement(@Param("threshold") Double threshold);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.depressionChange < :threshold AND o.status = 'COMPLETED'")
    List<InterventionOutcome> findWithDepressionImprovement(@Param("threshold") Double threshold);

    @Query("SELECT o FROM InterventionOutcome o WHERE o.anxietyChange < :threshold AND o.status = 'COMPLETED'")
    List<InterventionOutcome> findWithAnxietyImprovement(@Param("threshold") Double threshold);

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT AVG(o.stressChange), AVG(o.depressionChange), AVG(o.anxietyChange) " +
            "FROM InterventionOutcome o WHERE o.status = 'COMPLETED'")
    Object[] getAverageScoreChanges();

    @Query("SELECT AVG(o.stressChange), AVG(o.depressionChange), AVG(o.anxietyChange) " +
            "FROM InterventionOutcome o WHERE o.intervention.id = :interventionId AND o.status = 'COMPLETED'")
    Object[] getAverageScoreChangesForIntervention(@Param("interventionId") Long interventionId);

    @Query("SELECT o.responseType, COUNT(o) FROM InterventionOutcome o " +
            "WHERE o.status = 'COMPLETED' GROUP BY o.responseType")
    List<Object[]> getResponseTypeDistribution();

    @Query("SELECT AVG(o.userSatisfactionScore) FROM InterventionOutcome o " +
            "WHERE o.intervention.id = :interventionId AND o.userSatisfactionScore IS NOT NULL")
    Double getAverageSatisfactionForIntervention(@Param("interventionId") Long interventionId);

    // ==================== PAGINATION QUERIES ====================

    Page<InterventionOutcome> findByUserOrderByInterventionStartDateDesc(User user, Pageable pageable);

    Page<InterventionOutcome> findByIsSimulatedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<InterventionOutcome> findByStatusOrderByInterventionStartDateDesc(OutcomeStatus status, Pageable pageable);

    // ==================== REVIEW QUERIES ====================

    @Query("SELECT o FROM InterventionOutcome o WHERE o.status = 'PENDING_REVIEW'")
    List<InterventionOutcome> findPendingReview();

    @Query("SELECT o FROM InterventionOutcome o WHERE o.reviewedBy = :reviewer")
    List<InterventionOutcome> findReviewedBy(@Param("reviewer") String reviewer);

    // ==================== BATCH OPERATIONS ====================

    @Modifying
    @Query("UPDATE InterventionOutcome o SET o.status = :status WHERE o.id IN :ids")
    int bulkUpdateStatus(@Param("ids") List<Long> ids, @Param("status") OutcomeStatus status);

    @Modifying
    @Query("UPDATE InterventionOutcome o SET o.status = 'ARCHIVED' " +
            "WHERE o.interventionEndDate < :cutoffDate AND o.status = 'COMPLETED'")
    int archiveOldOutcomes(@Param("cutoffDate") LocalDateTime cutoffDate);
}

