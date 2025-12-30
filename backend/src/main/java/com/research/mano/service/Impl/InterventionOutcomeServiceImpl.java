package com.research.mano.service.Impl;

import com.research.mano.entity.Intervention;
import com.research.mano.entity.InterventionOutcome;
import com.research.mano.entity.InterventionOutcome.OutcomeType;
import com.research.mano.entity.InterventionOutcome.OutcomeStatus;
import com.research.mano.entity.InterventionOutcome.EffectivenessRating;
import com.research.mano.entity.InterventionOutcome.ResponseType;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.User;
import com.research.mano.repository.InterventionOutcomeRepository;
import com.research.mano.repository.InterventionRepository;
import com.research.mano.repository.UserRepository;
import com.research.mano.service.InterventionOutcomeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intervention Outcome Service Implementation for Component 1
 * Handles tracking, analysis, and simulation of intervention results
 */
@Service
@Transactional
public class InterventionOutcomeServiceImpl implements InterventionOutcomeService {

    @Autowired
    private InterventionOutcomeRepository outcomeRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== BASE SERVICE METHODS ====================

    @Override
    public InterventionOutcome save(InterventionOutcome outcome) {
        return outcomeRepository.save(outcome);
    }

    @Override
    public List<InterventionOutcome> saveAll(List<InterventionOutcome> entities) {
        return List.of();
    }

    @Override
    public Optional<InterventionOutcome> findById(Long id) {
        return outcomeRepository.findById(id);
    }

    @Override
    public List<InterventionOutcome> findAll() {
        return outcomeRepository.findAll();
    }

    @Override
    public Page<InterventionOutcome> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void deleteById(Long id) {
        outcomeRepository.deleteById(id);
    }

    @Override
    public void delete(InterventionOutcome outcome) {
        outcomeRepository.delete(outcome);
    }

    @Override
    public long count() {
        return outcomeRepository.count();
    }

    // ==================== BASIC QUERIES ====================

    @Override
    public Optional<InterventionOutcome> findByOutcomeCode(String outcomeCode) {
        return outcomeRepository.findByOutcomeCode(outcomeCode);
    }

    @Override
    public boolean existsByOutcomeCode(String outcomeCode) {
        return outcomeRepository.existsByOutcomeCode(outcomeCode);
    }

    @Override
    public List<InterventionOutcome> findByUser(User user) {
        return outcomeRepository.findByUser(user);
    }

    @Override
    public List<InterventionOutcome> findByUserId(Long userId) {
        return outcomeRepository.findByUserId(userId);
    }

    @Override
    public List<InterventionOutcome> findByIntervention(Intervention intervention) {
        return outcomeRepository.findByIntervention(intervention);
    }

    @Override
    public List<InterventionOutcome> findByInterventionId(Long interventionId) {
        return outcomeRepository.findByInterventionId(interventionId);
    }

    // ==================== STATUS AND TYPE QUERIES ====================

    @Override
    public List<InterventionOutcome> findByStatus(OutcomeStatus status) {
        return outcomeRepository.findByStatus(status);
    }

    @Override
    public List<InterventionOutcome> findByOutcomeType(OutcomeType outcomeType) {
        return outcomeRepository.findByOutcomeType(outcomeType);
    }

    @Override
    public List<InterventionOutcome> findByUserAndStatus(User user, OutcomeStatus status) {
        return outcomeRepository.findByUserAndStatus(user, status);
    }

    @Override
    public List<InterventionOutcome> findByUserIdAndStatus(Long userId, OutcomeStatus status) {
        return outcomeRepository.findByUserIdAndStatus(userId, status);
    }

    // ==================== USER INTERVENTION MANAGEMENT ====================

    @Override
    public InterventionOutcome startIntervention(User user, Intervention intervention,
                                                 Double preStressScore, Double preDepressionScore,
                                                 Double preAnxietyScore) {
        validateScores(preStressScore, preDepressionScore, preAnxietyScore);

        InterventionOutcome outcome = new InterventionOutcome(user, intervention,
                preStressScore, preDepressionScore, preAnxietyScore);

        // Set initial cluster based on scores
        outcome.setPreClusterIdentifier(determineClusterIdentifier(preStressScore, preDepressionScore, preAnxietyScore));

        // Set scheduled sessions based on intervention definition
        if (intervention.getDurationWeeks() != null && intervention.getFrequencyPerWeek() != null) {
            outcome.setSessionsScheduled(intervention.getDurationWeeks() * intervention.getFrequencyPerWeek());
            outcome.setSessionsCompleted(0);
        }

        return outcomeRepository.save(outcome);
    }

    @Override
    public InterventionOutcome startIntervention(Long userId, Long interventionId,
                                                 Double preStressScore, Double preDepressionScore,
                                                 Double preAnxietyScore) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + interventionId));

        return startIntervention(user, intervention, preStressScore, preDepressionScore, preAnxietyScore);
    }

    @Override
    public InterventionOutcome completeIntervention(Long outcomeId,
                                                    Double postStressScore, Double postDepressionScore,
                                                    Double postAnxietyScore) {
        validateScores(postStressScore, postDepressionScore, postAnxietyScore);

        InterventionOutcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Outcome not found with id: " + outcomeId));

        if (outcome.getStatus() != OutcomeStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete interventions that are in progress");
        }

        outcome.completeIntervention(postStressScore, postDepressionScore, postAnxietyScore);

        // Set post cluster
        outcome.setPostClusterIdentifier(determineClusterIdentifier(postStressScore, postDepressionScore, postAnxietyScore));

        // Calculate actual duration
        if (outcome.getInterventionStartDate() != null) {
            long days = java.time.Duration.between(outcome.getInterventionStartDate(), LocalDateTime.now()).toDays();
            outcome.setActualDurationWeeks((int) Math.ceil(days / 7.0));
        }

        return outcomeRepository.save(outcome);
    }

    @Override
    public InterventionOutcome recordDropout(Long outcomeId, String reason, Integer dropoutWeek) {
        InterventionOutcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Outcome not found with id: " + outcomeId));

        outcome.markAsDropout(reason, dropoutWeek);
        outcome.updateAdherence();

        return outcomeRepository.save(outcome);
    }

    @Override
    public InterventionOutcome updateAdherence(Long outcomeId, Integer sessionsCompleted, Integer sessionsScheduled) {
        InterventionOutcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Outcome not found with id: " + outcomeId));

        outcome.setSessionsCompleted(sessionsCompleted);
        if (sessionsScheduled != null) {
            outcome.setSessionsScheduled(sessionsScheduled);
        }
        outcome.updateAdherence();

        return outcomeRepository.save(outcome);
    }

    @Override
    public InterventionOutcome addUserFeedback(Long outcomeId, Integer satisfactionScore,
                                               String feedback, Boolean wouldRecommend) {
        InterventionOutcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Outcome not found with id: " + outcomeId));

        if (satisfactionScore != null && (satisfactionScore < 1 || satisfactionScore > 10)) {
            throw new IllegalArgumentException("Satisfaction score must be between 1 and 10");
        }

        outcome.setUserSatisfactionScore(satisfactionScore);
        outcome.setUserFeedback(feedback);
        outcome.setWouldRecommend(wouldRecommend);

        return outcomeRepository.save(outcome);
    }

    @Override
    public List<InterventionOutcome> getUserActiveInterventions(Long userId) {
        return outcomeRepository.findUserActiveInterventions(userId);
    }

    @Override
    public List<InterventionOutcome> getUserInterventionHistory(Long userId) {
        return outcomeRepository.findUserOutcomesChronologically(userId);
    }

    @Override
    public List<InterventionOutcome> getUserBestOutcomes(Long userId, Integer limit) {
        List<InterventionOutcome> bestOutcomes = outcomeRepository.findUserBestOutcomes(userId);
        if (limit != null && limit > 0) {
            return bestOutcomes.stream().limit(limit).collect(Collectors.toList());
        }
        return bestOutcomes;
    }

    @Override
    public Optional<InterventionOutcome> getLatestOutcome(User user) {
        return outcomeRepository.findLatestByUser(user);
    }

    // ==================== SIMULATION OPERATIONS (Component 1) ====================

    @Override
    public InterventionOutcome createSimulatedOutcome(Intervention intervention,
                                                      Double preStressScore, Double preDepressionScore,
                                                      Double preAnxietyScore,
                                                      Double postStressScore, Double postDepressionScore,
                                                      Double postAnxietyScore,
                                                      String simulationModelVersion) {
        validateScores(preStressScore, preDepressionScore, preAnxietyScore);
        validateScores(postStressScore, postDepressionScore, postAnxietyScore);

        InterventionOutcome outcome = new InterventionOutcome(intervention,
                preStressScore, preDepressionScore, preAnxietyScore, simulationModelVersion);

        // Set pre and post clusters
        outcome.setPreClusterIdentifier(determineClusterIdentifier(preStressScore, preDepressionScore, preAnxietyScore));
        outcome.setPostClusterIdentifier(determineClusterIdentifier(postStressScore, postDepressionScore, postAnxietyScore));

        // Complete with simulated post scores
        outcome.setPostStressScore(postStressScore);
        outcome.setPostDepressionScore(postDepressionScore);
        outcome.setPostAnxietyScore(postAnxietyScore);
        outcome.setPostOverallRisk((postStressScore + postDepressionScore + postAnxietyScore) / 3.0);

        outcome.calculateChanges();
        outcome.determineEffectiveness();

        // Simulate adherence (randomized but realistic)
        simulateAdherence(outcome, intervention);

        outcome.setAssessmentDate(LocalDateTime.now());
        outcome.setInterventionEndDate(LocalDateTime.now());

        return outcomeRepository.save(outcome);
    }

    private void simulateAdherence(InterventionOutcome outcome, Intervention intervention) {
        Random random = new Random();

        if (intervention.getDurationWeeks() != null && intervention.getFrequencyPerWeek() != null) {
            int totalSessions = intervention.getDurationWeeks() * intervention.getFrequencyPerWeek();
            outcome.setSessionsScheduled(totalSessions);

            // Simulate completion rate (normally distributed around 75%)
            double completionRate = Math.min(1.0, Math.max(0.3, 0.75 + random.nextGaussian() * 0.15));
            outcome.setSessionsCompleted((int) Math.round(totalSessions * completionRate));
            outcome.updateAdherence();

            // Simulate dropout based on completion rate
            if (completionRate < 0.5) {
                outcome.setDropout(true);
                outcome.setDropoutWeek(random.nextInt(intervention.getDurationWeeks()) + 1);
            }
        }

        // Simulate satisfaction (correlated with improvement)
        if (outcome.getOverallImprovementScore() != null) {
            double baseSatisfaction = 5.0 + (outcome.getOverallImprovementScore() * 10);
            int satisfaction = (int) Math.round(Math.min(10, Math.max(1, baseSatisfaction + random.nextGaussian())));
            outcome.setUserSatisfactionScore(satisfaction);
            outcome.setWouldRecommend(satisfaction >= 6);
        }
    }

    @Override
    public List<InterventionOutcome> createBulkSimulatedOutcomes(List<SimulatedOutcomeRequest> requests) {
        List<InterventionOutcome> outcomes = new ArrayList<>();

        for (SimulatedOutcomeRequest request : requests) {
            Intervention intervention = interventionRepository.findById(request.interventionId())
                    .orElseThrow(() -> new RuntimeException("Intervention not found: " + request.interventionId()));

            InterventionOutcome outcome = createSimulatedOutcome(
                    intervention,
                    request.preStressScore(), request.preDepressionScore(), request.preAnxietyScore(),
                    request.postStressScore(), request.postDepressionScore(), request.postAnxietyScore(),
                    request.simulationModelVersion()
            );

            if (request.confidenceScore() != null) {
                outcome.setConfidenceScore(request.confidenceScore());
            }
            if (request.noiseLevel() != null) {
                outcome.setNoiseLevel(request.noiseLevel());
            }

            outcomes.add(outcomeRepository.save(outcome));
        }

        return outcomes;
    }

    @Override
    public List<InterventionOutcome> getSimulatedOutcomes() {
        return outcomeRepository.findByIsSimulatedTrue();
    }

    @Override
    public List<InterventionOutcome> getSimulatedOutcomesByVersion(String modelVersion) {
        return outcomeRepository.findBySimulationVersion(modelVersion);
    }

    @Override
    public Map<String, Object> getSimulationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSimulated", outcomeRepository.countSimulatedOutcomes());
        stats.put("totalReal", outcomeRepository.countRealOutcomes());

        List<InterventionOutcome> simulated = getSimulatedOutcomes();
        if (!simulated.isEmpty()) {
            double avgImprovement = simulated.stream()
                    .filter(o -> o.getOverallImprovementScore() != null)
                    .mapToDouble(InterventionOutcome::getOverallImprovementScore)
                    .average()
                    .orElse(0.0);
            stats.put("avgSimulatedImprovement", avgImprovement);

            Map<String, Long> byVersion = simulated.stream()
                    .filter(o -> o.getSimulationModelVersion() != null)
                    .collect(Collectors.groupingBy(
                            InterventionOutcome::getSimulationModelVersion,
                            Collectors.counting()
                    ));
            stats.put("countByModelVersion", byVersion);
        }

        return stats;
    }

    // ==================== EFFECTIVENESS ANALYSIS ====================

    @Override
    public Double getAverageImprovementForIntervention(Long interventionId) {
        return outcomeRepository.getAverageImprovementForIntervention(interventionId);
    }

    @Override
    public Double getAverageImprovementByType(InterventionType type) {
        return outcomeRepository.getAverageImprovementByType(type);
    }

    @Override
    public List<Object[]> getEffectivenessByInterventionType() {
        return outcomeRepository.getEffectivenessByInterventionType();
    }

    @Override
    public List<Object[]> getEffectivenessDistribution(Long interventionId) {
        return outcomeRepository.getEffectivenessDistribution(interventionId);
    }

    @Override
    public List<InterventionOutcome> findByEffectivenessRating(EffectivenessRating rating) {
        return outcomeRepository.findByEffectivenessRating(rating);
    }

    @Override
    public List<InterventionOutcome> findByResponseType(ResponseType responseType) {
        return outcomeRepository.findByResponseType(responseType);
    }

    @Override
    public List<InterventionOutcome> findByMinImprovement(Double minImprovement) {
        return outcomeRepository.findByMinImprovement(minImprovement);
    }

    @Override
    public List<InterventionOutcome> getSuccessfulOutcomes() {
        return outcomeRepository.findSuccessfulOutcomes();
    }

    @Override
    public List<InterventionOutcome> getUnsuccessfulOutcomes() {
        return outcomeRepository.findUnsuccessfulOutcomes();
    }

    // ==================== ADHERENCE ANALYSIS ====================

    @Override
    public List<InterventionOutcome> getHighAdherenceOutcomes(Double minAdherence) {
        return outcomeRepository.findHighAdherenceOutcomes(minAdherence);
    }

    @Override
    public List<InterventionOutcome> getDropoutOutcomes() {
        return outcomeRepository.findByDropoutTrue();
    }

    @Override
    public Double getAverageAdherenceForIntervention(Long interventionId) {
        return outcomeRepository.getAverageAdherenceForIntervention(interventionId);
    }

    @Override
    public List<Object[]> getDropoutDistributionByWeek(Long interventionId) {
        return outcomeRepository.getDropoutDistributionByWeek(interventionId);
    }

    // ==================== CLUSTER ANALYSIS ====================

    @Override
    public List<InterventionOutcome> getOutcomesWithClusterTransitions() {
        return outcomeRepository.findWithClusterTransitions();
    }

    @Override
    public List<Object[]> getClusterTransitionMatrix() {
        return outcomeRepository.getClusterTransitionMatrix();
    }

    @Override
    public List<InterventionOutcome> findByPreCluster(String clusterId) {
        return outcomeRepository.findByPreCluster(clusterId);
    }

    @Override
    public InterventionOutcome updateClusterInfo(Long outcomeId, String preClusterId, String postClusterId) {
        InterventionOutcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Outcome not found with id: " + outcomeId));

        outcome.setPreClusterIdentifier(preClusterId);
        outcome.setPostClusterIdentifier(postClusterId);
        outcome.setClusterTransitionOccurred(!Objects.equals(preClusterId, postClusterId));

        return outcomeRepository.save(outcome);
    }

    // ==================== DATE RANGE QUERIES ====================

    @Override
    public List<InterventionOutcome> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return outcomeRepository.findByDateRange(start, end);
    }

    @Override
    public List<InterventionOutcome> findUserOutcomesByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return outcomeRepository.findUserOutcomesByDateRange(userId, start, end);
    }

    // ==================== STATISTICS ====================

    @Override
    public Object[] getAverageScoreChanges() {
        return outcomeRepository.getAverageScoreChanges();
    }

    @Override
    public Object[] getAverageScoreChangesForIntervention(Long interventionId) {
        return outcomeRepository.getAverageScoreChangesForIntervention(interventionId);
    }

    @Override
    public List<Object[]> getResponseTypeDistribution() {
        return outcomeRepository.getResponseTypeDistribution();
    }

    @Override
    public Double getAverageSatisfactionForIntervention(Long interventionId) {
        return outcomeRepository.getAverageSatisfactionForIntervention(interventionId);
    }

    @Override
    public Map<String, Object> getInterventionStatistics(Long interventionId) {
        Map<String, Object> stats = new HashMap<>();

        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new RuntimeException("Intervention not found: " + interventionId));

        stats.put("interventionName", intervention.getName());
        stats.put("interventionType", intervention.getInterventionType());

        List<InterventionOutcome> outcomes = findByInterventionId(interventionId);
        stats.put("totalOutcomes", outcomes.size());

        long completed = outcomes.stream().filter(o -> o.getStatus() == OutcomeStatus.COMPLETED).count();
        stats.put("completedOutcomes", completed);

        stats.put("averageImprovement", getAverageImprovementForIntervention(interventionId));
        stats.put("averageAdherence", getAverageAdherenceForIntervention(interventionId));
        stats.put("averageSatisfaction", getAverageSatisfactionForIntervention(interventionId));
        stats.put("effectivenessDistribution", getEffectivenessDistribution(interventionId));
        stats.put("dropoutByWeek", getDropoutDistributionByWeek(interventionId));
        stats.put("scoreChanges", getAverageScoreChangesForIntervention(interventionId));

        return stats;
    }

    @Override
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalOutcomes", count());
        stats.put("simulationStats", getSimulationStatistics());
        stats.put("effectivenessByType", getEffectivenessByInterventionType());
        stats.put("responseTypeDistribution", getResponseTypeDistribution());
        stats.put("averageScoreChanges", getAverageScoreChanges());
        stats.put("clusterTransitionMatrix", getClusterTransitionMatrix());

        return stats;
    }

    // ==================== PAGINATION ====================

    @Override
    public Page<InterventionOutcome> findByUserPaginated(User user, Pageable pageable) {
        return outcomeRepository.findByUserOrderByInterventionStartDateDesc(user, pageable);
    }

    @Override
    public Page<InterventionOutcome> findSimulatedPaginated(Pageable pageable) {
        return outcomeRepository.findByIsSimulatedTrueOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<InterventionOutcome> findByStatusPaginated(OutcomeStatus status, Pageable pageable) {
        return outcomeRepository.findByStatusOrderByInterventionStartDateDesc(status, pageable);
    }

    // ==================== REVIEW WORKFLOW ====================

    @Override
    public List<InterventionOutcome> getPendingReviewOutcomes() {
        return outcomeRepository.findPendingReview();
    }

    @Override
    public InterventionOutcome markAsReviewed(Long outcomeId, String reviewedBy, String notes) {
        InterventionOutcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new RuntimeException("Outcome not found with id: " + outcomeId));

        outcome.setReviewedBy(reviewedBy);
        outcome.setReviewDate(LocalDateTime.now());
        outcome.setNotes(notes);

        if (outcome.getStatus() == OutcomeStatus.PENDING_REVIEW) {
            outcome.setStatus(OutcomeStatus.COMPLETED);
        }

        return outcomeRepository.save(outcome);
    }

    @Override
    public List<InterventionOutcome> findReviewedBy(String reviewer) {
        return outcomeRepository.findReviewedBy(reviewer);
    }

    // ==================== BULK OPERATIONS ====================

    @Override
    public int bulkUpdateStatus(List<Long> ids, OutcomeStatus status) {
        return outcomeRepository.bulkUpdateStatus(ids, status);
    }

    @Override
    public int archiveOldOutcomes(LocalDateTime cutoffDate) {
        return outcomeRepository.archiveOldOutcomes(cutoffDate);
    }

    // ==================== HELPER METHODS ====================

    private void validateScores(Double stress, Double depression, Double anxiety) {
        if (stress == null || depression == null || anxiety == null) {
            throw new IllegalArgumentException("All scores must be provided");
        }
        if (stress < 0 || stress > 1 || depression < 0 || depression > 1 || anxiety < 0 || anxiety > 1) {
            throw new IllegalArgumentException("All scores must be between 0.0 and 1.0");
        }
    }

    private String determineClusterIdentifier(Double stress, Double depression, Double anxiety) {
        // Determine primary category
        String category;
        Double maxScore = Math.max(stress, Math.max(depression, anxiety));

        if (maxScore.equals(stress)) {
            category = "STRESS";
        } else if (maxScore.equals(depression)) {
            category = "DEPRESSION";
        } else {
            category = "ANXIETY";
        }

        // Determine level
        String level;
        if (maxScore >= 0.7) {
            level = "HIGH";
        } else if (maxScore >= 0.4) {
            level = "MEDIUM";
        } else {
            level = "LOW";
        }

        return category + "_" + level;
    }
}