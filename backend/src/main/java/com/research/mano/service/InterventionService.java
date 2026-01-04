package com.research.mano.service;

import com.research.mano.entity.Intervention;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.Intervention.IntensityLevel;
import com.research.mano.entity.Intervention.EvidenceLevel;
import com.research.mano.entity.Intervention.ValidationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Intervention Service Interface for Component 1 (AMISE)
 * Handles business logic for intervention management and recommendations
 */
public interface InterventionService extends BaseService<Intervention, Long> {

    // ==================== BASIC OPERATIONS ====================

    Optional<Intervention> findByCode(String interventionCode);

    Optional<Intervention> findByName(String name);

    boolean existsByCode(String interventionCode);

    List<Intervention> findAllActive();

    // ==================== TYPE AND INTENSITY QUERIES ====================

    List<Intervention> findByType(InterventionType type);

    List<Intervention> findByIntensity(IntensityLevel intensity);

    List<Intervention> findByTypeAndIntensity(InterventionType type, IntensityLevel intensity);

    List<Intervention> findActiveByType(InterventionType type);

    // ==================== EVIDENCE-BASED QUERIES ====================

    List<Intervention> findByEvidenceLevel(EvidenceLevel evidenceLevel);

    List<Intervention> findEvidenceBasedInterventions();

    List<Intervention> findValidatedInterventions();

    // ==================== RECOMMENDATION ENGINE ====================

    /**
     * Get personalized intervention recommendations based on user's mental health scores
     */
    List<Intervention> getRecommendationsForScores(Double stressScore, Double depressionScore,
                                                   Double anxietyScore, Integer maxResults);

    /**
     * Get recommendations with constraints (max duration, intensity, etc.)
     */
    List<Intervention> getRecommendationsWithConstraints(
            InterventionType preferredType,
            IntensityLevel maxIntensity,
            Integer maxDurationWeeks,
            Integer maxResults);

    /**
     * Get interventions recommended for a specific condition
     */
    List<Intervention> findRecommendedFor(String condition);

    /**
     * Get interventions without specific contraindication
     */
    List<Intervention> findWithoutContraindication(String condition);

    /**
     * Get crisis interventions for emergency situations
     */
    List<Intervention> getCrisisInterventions();

    // ==================== EFFECTIVENESS QUERIES ====================

    List<Intervention> findByMinStressReduction(Double minReduction);

    List<Intervention> findByMinDepressionReduction(Double minReduction);

    List<Intervention> findByMinAnxietyReduction(Double minReduction);

    List<Intervention> findByMinAverageReduction(Double minAvgReduction);

    // ==================== BINAURAL BEATS (Component 1 Feature) ====================

    List<Intervention> findBinauralInterventions();

    List<Intervention> findByBinauralFrequencyRange(Double minHz, Double maxHz);

    /**
     * Get binaural beats recommendation based on target mental state
     * Alpha (8-12 Hz): Relaxation
     * Theta (4-8 Hz): Deep relaxation, meditation
     * Delta (0.5-4 Hz): Deep sleep
     * Beta (12-30 Hz): Focus, alertness
     */
    List<Intervention> getBinauralRecommendation(String targetState);

    // ==================== SIMULATION SUPPORT (Component 1) ====================

    List<Intervention> findSimulatableInterventions();

    List<Intervention> findBySimulationModel(String modelId);

    // ==================== DURATION AND FREQUENCY ====================

    List<Intervention> findByMaxDuration(Integer maxWeeks);

    List<Intervention> findByDurationRange(Integer minWeeks, Integer maxWeeks);

    List<Intervention> findByMaxFrequency(Integer maxSessionsPerWeek);

    // ==================== SEARCH ====================

    List<Intervention> searchByKeyword(String keyword);

    // ==================== STATISTICS ====================

    List<Object[]> countByType();

    List<Object[]> countByIntensity();

    List<Object[]> countByEvidenceLevel();

    Object[] getAverageExpectedReductions();

    // ==================== MANAGEMENT OPERATIONS ====================

    Intervention createIntervention(Intervention intervention);

    Intervention updateIntervention(Long id, Intervention intervention);

    Intervention activateIntervention(Long id);

    Intervention deactivateIntervention(Long id);

    Intervention updateValidationStatus(Long id, ValidationStatus status, String reviewedBy);

    // ==================== BULK OPERATIONS ====================

    List<Intervention> createBulkInterventions(List<Intervention> interventions);

    int deactivateByType(InterventionType type);
}