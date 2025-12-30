package com.research.mano.repository;

import com.research.mano.entity.Intervention;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.Intervention.IntensityLevel;
import com.research.mano.entity.Intervention.EvidenceLevel;
import com.research.mano.entity.Intervention.ValidationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Intervention Repository for Component 1 (AMISE)
 * Provides data access for intervention definitions and queries
 */
@Repository
public interface InterventionRepository extends BaseRepository<Intervention, Long> {

    // ==================== BASIC QUERIES ====================

    Optional<Intervention> findByInterventionCode(String interventionCode);

    Optional<Intervention> findByName(String name);

    boolean existsByInterventionCode(String interventionCode);

    List<Intervention> findByIsActiveTrue();

    List<Intervention> findByIsActiveFalse();

    // ==================== TYPE AND INTENSITY QUERIES ====================

    List<Intervention> findByInterventionType(InterventionType interventionType);

    List<Intervention> findByIntensityLevel(IntensityLevel intensityLevel);

    List<Intervention> findByInterventionTypeAndIntensityLevel(
            InterventionType interventionType, IntensityLevel intensityLevel);

    List<Intervention> findByInterventionTypeIn(List<InterventionType> types);

    @Query("SELECT i FROM Intervention i WHERE i.interventionType = :type AND i.isActive = true ORDER BY i.evidenceLevel DESC")
    List<Intervention> findActiveByType(@Param("type") InterventionType type);

    // ==================== EVIDENCE-BASED QUERIES ====================

    List<Intervention> findByEvidenceLevel(EvidenceLevel evidenceLevel);

    @Query("SELECT i FROM Intervention i WHERE i.evidenceLevel IN ('STRONG', 'MODERATE') AND i.isActive = true")
    List<Intervention> findEvidenceBasedInterventions();

    List<Intervention> findByValidationStatus(ValidationStatus validationStatus);

    @Query("SELECT i FROM Intervention i WHERE i.validationStatus = 'VALIDATED' AND i.isActive = true")
    List<Intervention> findValidatedInterventions();

    // ==================== EFFECTIVENESS QUERIES ====================

    @Query("SELECT i FROM Intervention i WHERE i.expectedStressReduction >= :minReduction AND i.isActive = true")
    List<Intervention> findByMinStressReduction(@Param("minReduction") Double minReduction);

    @Query("SELECT i FROM Intervention i WHERE i.expectedDepressionReduction >= :minReduction AND i.isActive = true")
    List<Intervention> findByMinDepressionReduction(@Param("minReduction") Double minReduction);

    @Query("SELECT i FROM Intervention i WHERE i.expectedAnxietyReduction >= :minReduction AND i.isActive = true")
    List<Intervention> findByMinAnxietyReduction(@Param("minReduction") Double minReduction);

    @Query("SELECT i FROM Intervention i WHERE " +
            "(i.expectedStressReduction + i.expectedDepressionReduction + i.expectedAnxietyReduction) / 3.0 >= :minAvgReduction " +
            "AND i.isActive = true ORDER BY (i.expectedStressReduction + i.expectedDepressionReduction + i.expectedAnxietyReduction) DESC")
    List<Intervention> findByMinAverageReduction(@Param("minAvgReduction") Double minAvgReduction);

    // ==================== DURATION AND FREQUENCY QUERIES ====================

    List<Intervention> findByDurationWeeksLessThanEqual(Integer maxWeeks);

    List<Intervention> findByDurationWeeksBetween(Integer minWeeks, Integer maxWeeks);

    @Query("SELECT i FROM Intervention i WHERE i.frequencyPerWeek <= :maxFrequency AND i.isActive = true")
    List<Intervention> findByMaxFrequency(@Param("maxFrequency") Integer maxFrequency);

    @Query("SELECT i FROM Intervention i WHERE i.sessionDurationMinutes <= :maxMinutes AND i.isActive = true")
    List<Intervention> findByMaxSessionDuration(@Param("maxMinutes") Integer maxMinutes);

    // ==================== BINAURAL BEATS QUERIES (Component 1 Feature) ====================

    @Query("SELECT i FROM Intervention i WHERE i.binauralFrequencyHz IS NOT NULL AND i.isActive = true")
    List<Intervention> findBinauralInterventions();

    @Query("SELECT i FROM Intervention i WHERE i.binauralFrequencyHz BETWEEN :minHz AND :maxHz AND i.isActive = true")
    List<Intervention> findByBinauralFrequencyRange(
            @Param("minHz") Double minHz, @Param("maxHz") Double maxHz);

    @Query("SELECT i FROM Intervention i WHERE i.interventionType = 'BINAURAL_BEATS' AND i.isActive = true")
    List<Intervention> findActiveBinauralBeatsInterventions();

    // ==================== RECOMMENDATION QUERIES ====================

    @Query("SELECT i FROM Intervention i WHERE i.recommendedFor LIKE %:condition% AND i.isActive = true")
    List<Intervention> findRecommendedFor(@Param("condition") String condition);

    @Query("SELECT i FROM Intervention i WHERE i.contraindications NOT LIKE %:condition% AND i.isActive = true")
    List<Intervention> findWithoutContraindication(@Param("condition") String condition);

    // ==================== SIMULATION QUERIES (Component 1) ====================

    @Query("SELECT i FROM Intervention i WHERE i.simulationModelId IS NOT NULL AND i.isActive = true")
    List<Intervention> findSimulatableInterventions();

    @Query("SELECT i FROM Intervention i WHERE i.simulationModelId = :modelId")
    List<Intervention> findBySimulationModel(@Param("modelId") String modelId);

    // ==================== CRISIS INTERVENTION QUERIES ====================

    @Query("SELECT i FROM Intervention i WHERE i.interventionType IN ('CRISIS_SUPPORT', 'EMERGENCY_HOTLINE') AND i.isActive = true")
    List<Intervention> findCrisisInterventions();

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT i.interventionType, COUNT(i) FROM Intervention i WHERE i.isActive = true GROUP BY i.interventionType")
    List<Object[]> countByInterventionType();

    @Query("SELECT i.intensityLevel, COUNT(i) FROM Intervention i WHERE i.isActive = true GROUP BY i.intensityLevel")
    List<Object[]> countByIntensityLevel();

    @Query("SELECT i.evidenceLevel, COUNT(i) FROM Intervention i WHERE i.isActive = true GROUP BY i.evidenceLevel")
    List<Object[]> countByEvidenceLevel();

    @Query("SELECT AVG(i.expectedStressReduction), AVG(i.expectedDepressionReduction), AVG(i.expectedAnxietyReduction) " +
            "FROM Intervention i WHERE i.isActive = true")
    Object[] getAverageExpectedReductions();

    // ==================== SEARCH QUERIES ====================

    @Query("SELECT i FROM Intervention i WHERE " +
            "(LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND i.isActive = true")
    List<Intervention> searchByKeyword(@Param("keyword") String keyword);

    // ==================== COMPLEX RECOMMENDATION QUERY ====================

    @Query("SELECT i FROM Intervention i WHERE " +
            "i.isActive = true AND " +
            "i.validationStatus = 'VALIDATED' AND " +
            "i.evidenceLevel IN ('STRONG', 'MODERATE') AND " +
            "(:type IS NULL OR i.interventionType = :type) AND " +
            "(:maxIntensity IS NULL OR i.intensityLevel <= :maxIntensity) AND " +
            "(:maxDuration IS NULL OR i.durationWeeks <= :maxDuration) " +
            "ORDER BY (i.expectedStressReduction + i.expectedDepressionReduction + i.expectedAnxietyReduction) DESC")
    List<Intervention> findRecommendedInterventions(
            @Param("type") InterventionType type,
            @Param("maxIntensity") IntensityLevel maxIntensity,
            @Param("maxDuration") Integer maxDuration);
}