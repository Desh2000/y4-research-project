package com.research.mano.service.Impl;

import com.research.mano.entity.Intervention;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.Intervention.IntensityLevel;
import com.research.mano.entity.Intervention.EvidenceLevel;
import com.research.mano.entity.Intervention.ValidationStatus;
import com.research.mano.repository.InterventionRepository;
import com.research.mano.service.InterventionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intervention Service Implementation for Component 1 (AMISE)
 * Implements business logic for intervention management and smart recommendations
 */
@Service
@Transactional
public class InterventionServiceImpl implements InterventionService {

    @Autowired
    private InterventionRepository interventionRepository;

    // ==================== BASE SERVICE METHODS ====================

    @Override
    public Intervention save(Intervention intervention) {
        return interventionRepository.save(intervention);
    }

    @Override
    public List<Intervention> saveAll(List<Intervention> entities) {
        return List.of();
    }

    @Override
    public Optional<Intervention> findById(Long id) {
        return interventionRepository.findById(id);
    }

    @Override
    public List<Intervention> findAll() {
        return interventionRepository.findAll();
    }

    @Override
    public Page<Intervention> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void deleteById(Long id) {
        interventionRepository.deleteById(id);
    }

    @Override
    public void delete(Intervention intervention) {
        interventionRepository.delete(intervention);
    }

    @Override
    public long count() {
        return interventionRepository.count();
    }

    // ==================== BASIC OPERATIONS ====================

    @Override
    public Optional<Intervention> findByCode(String interventionCode) {
        return interventionRepository.findByInterventionCode(interventionCode);
    }

    @Override
    public Optional<Intervention> findByName(String name) {
        return interventionRepository.findByName(name);
    }

    @Override
    public boolean existsByCode(String interventionCode) {
        return interventionRepository.existsByInterventionCode(interventionCode);
    }

    @Override
    public List<Intervention> findAllActive() {
        return interventionRepository.findByIsActiveTrue();
    }

    // ==================== TYPE AND INTENSITY QUERIES ====================

    @Override
    public List<Intervention> findByType(InterventionType type) {
        return interventionRepository.findByInterventionType(type);
    }

    @Override
    public List<Intervention> findByIntensity(IntensityLevel intensity) {
        return interventionRepository.findByIntensityLevel(intensity);
    }

    @Override
    public List<Intervention> findByTypeAndIntensity(InterventionType type, IntensityLevel intensity) {
        return interventionRepository.findByInterventionTypeAndIntensityLevel(type, intensity);
    }

    @Override
    public List<Intervention> findActiveByType(InterventionType type) {
        return interventionRepository.findActiveByType(type);
    }

    // ==================== EVIDENCE-BASED QUERIES ====================

    @Override
    public List<Intervention> findByEvidenceLevel(EvidenceLevel evidenceLevel) {
        return interventionRepository.findByEvidenceLevel(evidenceLevel);
    }

    @Override
    public List<Intervention> findEvidenceBasedInterventions() {
        return interventionRepository.findEvidenceBasedInterventions();
    }

    @Override
    public List<Intervention> findValidatedInterventions() {
        return interventionRepository.findValidatedInterventions();
    }

    // ==================== RECOMMENDATION ENGINE ====================

    @Override
    public List<Intervention> getRecommendationsForScores(Double stressScore, Double depressionScore,
                                                          Double anxietyScore, Integer maxResults) {
        List<Intervention> allActive = findAllActive();

        // Determine primary concern based on highest score
        String primaryConcern = determinePrimaryConcern(stressScore, depressionScore, anxietyScore);
        Double highestScore = Math.max(stressScore, Math.max(depressionScore, anxietyScore));

        // Filter and score interventions
        List<ScoredIntervention> scoredInterventions = allActive.stream()
                .filter(i -> i.getValidationStatus() == ValidationStatus.VALIDATED)
                .map(intervention -> scoreIntervention(intervention, stressScore, depressionScore,
                        anxietyScore, primaryConcern, highestScore))
                .sorted(Comparator.comparingDouble(ScoredIntervention::score).reversed())
                .toList();

        int limit = maxResults != null ? maxResults : 10;
        return scoredInterventions.stream()
                .limit(limit)
                .map(ScoredIntervention::intervention)
                .collect(Collectors.toList());
    }

    private String determinePrimaryConcern(Double stress, Double depression, Double anxiety) {
        if (stress >= depression && stress >= anxiety) return "STRESS";
        if (depression >= stress && depression >= anxiety) return "DEPRESSION";
        return "ANXIETY";
    }

    private ScoredIntervention scoreIntervention(Intervention intervention, Double stressScore,
                                                 Double depressionScore, Double anxietyScore,
                                                 String primaryConcern, Double severityScore) {
        double score = 0.0;

        // Base score from expected reductions
        Double expectedStress = intervention.getExpectedStressReduction();
        Double expectedDepression = intervention.getExpectedDepressionReduction();
        Double expectedAnxiety = intervention.getExpectedAnxietyReduction();

        if (expectedStress != null) score += expectedStress * stressScore * 100;
        if (expectedDepression != null) score += expectedDepression * depressionScore * 100;
        if (expectedAnxiety != null) score += expectedAnxiety * anxietyScore * 100;

        // Bonus for matching primary concern
        switch (primaryConcern) {
            case "STRESS" -> {
                if (expectedStress != null && expectedStress >= 0.15) score += 20;
            }
            case "DEPRESSION" -> {
                if (expectedDepression != null && expectedDepression >= 0.15) score += 20;
            }
            case "ANXIETY" -> {
                if (expectedAnxiety != null && expectedAnxiety >= 0.15) score += 20;
            }
        }

        // Evidence level bonus
        if (intervention.getEvidenceLevel() == EvidenceLevel.STRONG) score += 15;
        else if (intervention.getEvidenceLevel() == EvidenceLevel.MODERATE) score += 10;

        // Adjust intensity recommendation based on severity
        IntensityLevel recommendedIntensity = getRecommendedIntensity(severityScore);
        if (intervention.getIntensityLevel() == recommendedIntensity) score += 10;

        // Crisis intervention bonus for high severity
        if (severityScore >= 0.8 && intervention.isSuitableForCrisis()) score += 25;

        return new ScoredIntervention(intervention, score);
    }

    private IntensityLevel getRecommendedIntensity(Double severityScore) {
        if (severityScore >= 0.8) return IntensityLevel.HIGH;
        if (severityScore >= 0.5) return IntensityLevel.MEDIUM;
        if (severityScore >= 0.3) return IntensityLevel.LOW;
        return IntensityLevel.MINIMAL;
    }

    private record ScoredIntervention(Intervention intervention, double score) {}

    @Override
    public List<Intervention> getRecommendationsWithConstraints(
            InterventionType preferredType,
            IntensityLevel maxIntensity,
            Integer maxDurationWeeks,
            Integer maxResults) {
        return interventionRepository.findRecommendedInterventions(preferredType, maxIntensity, maxDurationWeeks)
                .stream()
                .limit(maxResults != null ? maxResults : 10)
                .collect(Collectors.toList());
    }

    @Override
    public List<Intervention> findRecommendedFor(String condition) {
        return interventionRepository.findRecommendedFor(condition);
    }

    @Override
    public List<Intervention> findWithoutContraindication(String condition) {
        return interventionRepository.findWithoutContraindication(condition);
    }

    @Override
    public List<Intervention> getCrisisInterventions() {
        return interventionRepository.findCrisisInterventions();
    }

    // ==================== EFFECTIVENESS QUERIES ====================

    @Override
    public List<Intervention> findByMinStressReduction(Double minReduction) {
        return interventionRepository.findByMinStressReduction(minReduction);
    }

    @Override
    public List<Intervention> findByMinDepressionReduction(Double minReduction) {
        return interventionRepository.findByMinDepressionReduction(minReduction);
    }

    @Override
    public List<Intervention> findByMinAnxietyReduction(Double minReduction) {
        return interventionRepository.findByMinAnxietyReduction(minReduction);
    }

    @Override
    public List<Intervention> findByMinAverageReduction(Double minAvgReduction) {
        return interventionRepository.findByMinAverageReduction(minAvgReduction);
    }

    // ==================== BINAURAL BEATS (Component 1 Feature) ====================

    @Override
    public List<Intervention> findBinauralInterventions() {
        return interventionRepository.findBinauralInterventions();
    }

    @Override
    public List<Intervention> findByBinauralFrequencyRange(Double minHz, Double maxHz) {
        return interventionRepository.findByBinauralFrequencyRange(minHz, maxHz);
    }

    @Override
    public List<Intervention> getBinauralRecommendation(String targetState) {
        // Frequency ranges for different mental states
        return switch (targetState.toUpperCase()) {
            case "RELAXATION", "CALM" -> findByBinauralFrequencyRange(8.0, 12.0);  // Alpha waves
            case "MEDITATION", "DEEP_RELAXATION" -> findByBinauralFrequencyRange(4.0, 8.0);   // Theta waves
            case "SLEEP", "DEEP_SLEEP" -> findByBinauralFrequencyRange(0.5, 4.0);  // Delta waves
            case "FOCUS", "ALERTNESS", "CONCENTRATION" -> findByBinauralFrequencyRange(12.0, 30.0); // Beta waves
            case "CREATIVITY" -> findByBinauralFrequencyRange(7.0, 13.0); // Alpha-Theta border
            default -> findBinauralInterventions();
        };
    }

    // ==================== SIMULATION SUPPORT (Component 1) ====================

    @Override
    public List<Intervention> findSimulatableInterventions() {
        return interventionRepository.findSimulatableInterventions();
    }

    @Override
    public List<Intervention> findBySimulationModel(String modelId) {
        return interventionRepository.findBySimulationModel(modelId);
    }

    // ==================== DURATION AND FREQUENCY ====================

    @Override
    public List<Intervention> findByMaxDuration(Integer maxWeeks) {
        return interventionRepository.findByDurationWeeksLessThanEqual(maxWeeks);
    }

    @Override
    public List<Intervention> findByDurationRange(Integer minWeeks, Integer maxWeeks) {
        return interventionRepository.findByDurationWeeksBetween(minWeeks, maxWeeks);
    }

    @Override
    public List<Intervention> findByMaxFrequency(Integer maxSessionsPerWeek) {
        return interventionRepository.findByMaxFrequency(maxSessionsPerWeek);
    }

    // ==================== SEARCH ====================

    @Override
    public List<Intervention> searchByKeyword(String keyword) {
        return interventionRepository.searchByKeyword(keyword);
    }

    // ==================== STATISTICS ====================

    @Override
    public List<Object[]> countByType() {
        return interventionRepository.countByInterventionType();
    }

    @Override
    public List<Object[]> countByIntensity() {
        return interventionRepository.countByIntensityLevel();
    }

    @Override
    public List<Object[]> countByEvidenceLevel() {
        return interventionRepository.countByEvidenceLevel();
    }

    @Override
    public Object[] getAverageExpectedReductions() {
        return interventionRepository.getAverageExpectedReductions();
    }

    // ==================== MANAGEMENT OPERATIONS ====================

    @Override
    public Intervention createIntervention(Intervention intervention) {
        // Generate code if not provided
        if (intervention.getInterventionCode() == null || intervention.getInterventionCode().isEmpty()) {
            String code = generateInterventionCode(intervention);
            intervention.setInterventionCode(code);
        }

        // Set defaults
        if (intervention.getIsActive() == null) {
            intervention.setIsActive(true);
        }
        if (intervention.getValidationStatus() == null) {
            intervention.setValidationStatus(ValidationStatus.PENDING);
        }

        return interventionRepository.save(intervention);
    }

    private String generateInterventionCode(Intervention intervention) {
        String typeCode = intervention.getInterventionType() != null
                ? intervention.getInterventionType().name().substring(0, Math.min(3, intervention.getInterventionType().name().length()))
                : "INT";
        String intensityCode = intervention.getIntensityLevel() != null
                ? intervention.getIntensityLevel().name().substring(0, 1)
                : "M";
        long timestamp = System.currentTimeMillis() % 100000;
        return String.format("%s_%s_%05d", typeCode, intensityCode, timestamp);
    }

    @Override
    public Intervention updateIntervention(Long id, Intervention updatedIntervention) {
        Intervention existing = interventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + id));

        // Update fields
        if (updatedIntervention.getName() != null) {
            existing.setName(updatedIntervention.getName());
        }
        if (updatedIntervention.getDescription() != null) {
            existing.setDescription(updatedIntervention.getDescription());
        }
        if (updatedIntervention.getInterventionType() != null) {
            existing.setInterventionType(updatedIntervention.getInterventionType());
        }
        if (updatedIntervention.getIntensityLevel() != null) {
            existing.setIntensityLevel(updatedIntervention.getIntensityLevel());
        }
        if (updatedIntervention.getDurationWeeks() != null) {
            existing.setDurationWeeks(updatedIntervention.getDurationWeeks());
        }
        if (updatedIntervention.getFrequencyPerWeek() != null) {
            existing.setFrequencyPerWeek(updatedIntervention.getFrequencyPerWeek());
        }
        if (updatedIntervention.getSessionDurationMinutes() != null) {
            existing.setSessionDurationMinutes(updatedIntervention.getSessionDurationMinutes());
        }

        // Update expected effects
        if (updatedIntervention.getExpectedStressReduction() != null) {
            existing.setExpectedStressReduction(updatedIntervention.getExpectedStressReduction());
        }
        if (updatedIntervention.getExpectedDepressionReduction() != null) {
            existing.setExpectedDepressionReduction(updatedIntervention.getExpectedDepressionReduction());
        }
        if (updatedIntervention.getExpectedAnxietyReduction() != null) {
            existing.setExpectedAnxietyReduction(updatedIntervention.getExpectedAnxietyReduction());
        }
        if (updatedIntervention.getExpectedResilienceIncrease() != null) {
            existing.setExpectedResilienceIncrease(updatedIntervention.getExpectedResilienceIncrease());
        }

        // Update binaural fields
        if (updatedIntervention.getBinauralFrequencyHz() != null) {
            existing.setBinauralFrequencyHz(updatedIntervention.getBinauralFrequencyHz());
        }
        if (updatedIntervention.getBinauralSessionMinutes() != null) {
            existing.setBinauralSessionMinutes(updatedIntervention.getBinauralSessionMinutes());
        }

        // Update evidence and validation
        if (updatedIntervention.getEvidenceLevel() != null) {
            existing.setEvidenceLevel(updatedIntervention.getEvidenceLevel());
        }
        if (updatedIntervention.getResearchReferences() != null) {
            existing.setResearchReferences(updatedIntervention.getResearchReferences());
        }

        // Update contraindications and recommendations
        if (updatedIntervention.getContraindications() != null) {
            existing.setContraindications(updatedIntervention.getContraindications());
        }
        if (updatedIntervention.getPrerequisites() != null) {
            existing.setPrerequisites(updatedIntervention.getPrerequisites());
        }
        if (updatedIntervention.getRecommendedFor() != null) {
            existing.setRecommendedFor(updatedIntervention.getRecommendedFor());
        }

        // Update simulation parameters
        if (updatedIntervention.getSimulationModelId() != null) {
            existing.setSimulationModelId(updatedIntervention.getSimulationModelId());
        }
        if (updatedIntervention.getSimulationParameters() != null) {
            existing.setSimulationParameters(updatedIntervention.getSimulationParameters());
        }

        return interventionRepository.save(existing);
    }

    @Override
    public Intervention activateIntervention(Long id) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + id));
        intervention.setIsActive(true);
        return interventionRepository.save(intervention);
    }

    @Override
    public Intervention deactivateIntervention(Long id) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + id));
        intervention.setIsActive(false);
        return interventionRepository.save(intervention);
    }

    @Override
    public Intervention updateValidationStatus(Long id, ValidationStatus status, String reviewedBy) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + id));
        intervention.setValidationStatus(status);
        intervention.setCreatedBy(reviewedBy);
        intervention.setLastValidated(LocalDateTime.now());
        return interventionRepository.save(intervention);
    }

    // ==================== BULK OPERATIONS ====================

    @Override
    public List<Intervention> createBulkInterventions(List<Intervention> interventions) {
        interventions.forEach(i -> {
            if (i.getInterventionCode() == null || i.getInterventionCode().isEmpty()) {
                i.setInterventionCode(generateInterventionCode(i));
            }
            if (i.getIsActive() == null) i.setIsActive(true);
            if (i.getValidationStatus() == null) i.setValidationStatus(ValidationStatus.PENDING);
        });
        return interventionRepository.saveAll(interventions);
    }

    @Override
    public int deactivateByType(InterventionType type) {
        List<Intervention> interventions = interventionRepository.findByInterventionType(type);
        interventions.forEach(i -> i.setIsActive(false));
        interventionRepository.saveAll(interventions);
        return interventions.size();
    }
}