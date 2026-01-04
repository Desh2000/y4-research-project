package com.research.mano.service;

import com.research.mano.entity.Intervention;
import com.research.mano.entity.InterventionOutcome;
import com.research.mano.entity.InterventionOutcome.OutcomeType;
import com.research.mano.entity.InterventionOutcome.OutcomeStatus;
import com.research.mano.entity.InterventionOutcome.EffectivenessRating;
import com.research.mano.entity.InterventionOutcome.ResponseType;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Intervention Outcome Service Interface for Component 1
 * Handles business logic for tracking and analyzing intervention results
 */
public interface InterventionOutcomeService extends BaseService<InterventionOutcome, Long> {

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

    List<InterventionOutcome> findByUserIdAndStatus(Long userId, OutcomeStatus status);

    // ==================== USER INTERVENTION MANAGEMENT ====================

    /**
     * Start a new intervention for a user
     */
    InterventionOutcome startIntervention(User user, Intervention intervention,
                                          Double preStressScore, Double preDepressionScore,
                                          Double preAnxietyScore);

    /**
     * Start intervention by IDs
     */
    InterventionOutcome startIntervention(Long userId, Long interventionId,
                                          Double preStressScore, Double preDepressionScore,
                                          Double preAnxietyScore);

    /**
     * Complete an intervention with post-assessment scores
     */
    InterventionOutcome completeIntervention(Long outcomeId,
                                             Double postStressScore, Double postDepressionScore,
                                             Double postAnxietyScore);

    /**
     * Record user dropping out of intervention
     */
    InterventionOutcome recordDropout(Long outcomeId, String reason, Integer dropoutWeek);

    /**
     * Update adherence metrics
     */
    InterventionOutcome updateAdherence(Long outcomeId, Integer sessionsCompleted, Integer sessionsScheduled);

    /**
     * Add user feedback to completed intervention
     */
    InterventionOutcome addUserFeedback(Long outcomeId, Integer satisfactionScore,
                                        String feedback, Boolean wouldRecommend);

    /**
     * Get user's active (in-progress) interventions
     */
    List<InterventionOutcome> getUserActiveInterventions(Long userId);

    /**
     * Get user's intervention history
     */
    List<InterventionOutcome> getUserInterventionHistory(Long userId);

    /**
     * Get user's most successful interventions
     */
    List<InterventionOutcome> getUserBestOutcomes(Long userId, Integer limit);

    /**
     * Get user's latest intervention outcome
     */
    Optional<InterventionOutcome> getLatestOutcome(User user);

    // ==================== SIMULATION OPERATIONS (Component 1) ====================

    /**
     * Create a simulated intervention outcome
     */
    InterventionOutcome createSimulatedOutcome(Intervention intervention,
                                               Double preStressScore, Double preDepressionScore,
                                               Double preAnxietyScore,
                                               Double postStressScore, Double postDepressionScore,
                                               Double postAnxietyScore,
                                               String simulationModelVersion);

    /**
     * Bulk create simulated outcomes for training data
     */
    List<InterventionOutcome> createBulkSimulatedOutcomes(List<SimulatedOutcomeRequest> requests);

    /**
     * Get all simulated outcomes
     */
    List<InterventionOutcome> getSimulatedOutcomes();

    /**
     * Get simulated outcomes by model version
     */
    List<InterventionOutcome> getSimulatedOutcomesByVersion(String modelVersion);

    /**
     * Get simulation statistics
     */
    Map<String, Object> getSimulationStatistics();

    // ==================== EFFECTIVENESS ANALYSIS ====================

    /**
     * Get average improvement for an intervention
     */
    Double getAverageImprovementForIntervention(Long interventionId);

    /**
     * Get average improvement by intervention type
     */
    Double getAverageImprovementByType(InterventionType type);

    /**
     * Get effectiveness breakdown by intervention type
     */
    List<Object[]> getEffectivenessByInterventionType();

    /**
     * Get effectiveness distribution for an intervention
     */
    List<Object[]> getEffectivenessDistribution(Long interventionId);

    /**
     * Find outcomes by effectiveness rating
     */
    List<InterventionOutcome> findByEffectivenessRating(EffectivenessRating rating);

    /**
     * Find outcomes by response type
     */
    List<InterventionOutcome> findByResponseType(ResponseType responseType);

    /**
     * Get outcomes with minimum improvement threshold
     */
    List<InterventionOutcome> findByMinImprovement(Double minImprovement);

    /**
     * Get successful outcomes (met expected results)
     */
    List<InterventionOutcome> getSuccessfulOutcomes();

    /**
     * Get unsuccessful outcomes
     */
    List<InterventionOutcome> getUnsuccessfulOutcomes();

    // ==================== ADHERENCE ANALYSIS ====================

    /**
     * Get high adherence outcomes
     */
    List<InterventionOutcome> getHighAdherenceOutcomes(Double minAdherence);

    /**
     * Get dropout outcomes
     */
    List<InterventionOutcome> getDropoutOutcomes();

    /**
     * Get average adherence for an intervention
     */
    Double getAverageAdherenceForIntervention(Long interventionId);

    /**
     * Get dropout distribution by week for an intervention
     */
    List<Object[]> getDropoutDistributionByWeek(Long interventionId);

    // ==================== CLUSTER ANALYSIS ====================

    /**
     * Get outcomes with cluster transitions
     */
    List<InterventionOutcome> getOutcomesWithClusterTransitions();

    /**
     * Get cluster transition matrix
     */
    List<Object[]> getClusterTransitionMatrix();

    /**
     * Get outcomes by pre-intervention cluster
     */
    List<InterventionOutcome> findByPreCluster(String clusterId);

    /**
     * Update cluster information for an outcome
     */
    InterventionOutcome updateClusterInfo(Long outcomeId, String preClusterId, String postClusterId);

    // ==================== DATE RANGE QUERIES ====================

    List<InterventionOutcome> findByDateRange(LocalDateTime start, LocalDateTime end);

    List<InterventionOutcome> findUserOutcomesByDateRange(Long userId, LocalDateTime start, LocalDateTime end);

    // ==================== STATISTICS ====================

    /**
     * Get average score changes across all completed outcomes
     */
    Object[] getAverageScoreChanges();

    /**
     * Get average score changes for specific intervention
     */
    Object[] getAverageScoreChangesForIntervention(Long interventionId);

    /**
     * Get response type distribution
     */
    List<Object[]> getResponseTypeDistribution();

    /**
     * Get average satisfaction for an intervention
     */
    Double getAverageSatisfactionForIntervention(Long interventionId);

    /**
     * Get comprehensive statistics for an intervention
     */
    Map<String, Object> getInterventionStatistics(Long interventionId);

    /**
     * Get overall system statistics
     */
    Map<String, Object> getOverallStatistics();

    // ==================== PAGINATION ====================

    Page<InterventionOutcome> findByUserPaginated(User user, Pageable pageable);

    Page<InterventionOutcome> findSimulatedPaginated(Pageable pageable);

    Page<InterventionOutcome> findByStatusPaginated(OutcomeStatus status, Pageable pageable);

    // ==================== REVIEW WORKFLOW ====================

    /**
     * Get outcomes pending review
     */
    List<InterventionOutcome> getPendingReviewOutcomes();

    /**
     * Mark outcome as reviewed
     */
    InterventionOutcome markAsReviewed(Long outcomeId, String reviewedBy, String notes);

    /**
     * Get outcomes reviewed by specific reviewer
     */
    List<InterventionOutcome> findReviewedBy(String reviewer);

    // ==================== BULK OPERATIONS ====================

    /**
     * Bulk update status
     */
    int bulkUpdateStatus(List<Long> ids, OutcomeStatus status);

    /**
     * Archive old completed outcomes
     */
    int archiveOldOutcomes(LocalDateTime cutoffDate);

    // ==================== DATA TRANSFER OBJECT ====================

    /**
     * Request object for creating simulated outcomes
     */
    record SimulatedOutcomeRequest(
            Long interventionId,
            Double preStressScore,
            Double preDepressionScore,
            Double preAnxietyScore,
            Double postStressScore,
            Double postDepressionScore,
            Double postAnxietyScore,
            String simulationModelVersion,
            Double confidenceScore,
            Double noiseLevel
    ) {}
}