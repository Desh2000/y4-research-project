package com.research.mano.controller.responce;

import com.research.mano.entity.Intervention;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.Intervention.IntensityLevel;
import com.research.mano.entity.Intervention.EvidenceLevel;
import com.research.mano.entity.Intervention.ValidationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Intervention entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterventionDTO {

    private Long id;
    private String interventionCode;
    private String name;
    private String description;
    private InterventionType interventionType;
    private String interventionTypeDisplayName;
    private IntensityLevel intensityLevel;
    private String intensityLevelDescription;

    // Duration and frequency
    private Integer durationWeeks;
    private Integer frequencyPerWeek;
    private Integer sessionDurationMinutes;
    private Integer totalSessions;

    // Expected effects
    private Double expectedStressReduction;
    private Double expectedDepressionReduction;
    private Double expectedAnxietyReduction;
    private Double expectedResilienceIncrease;
    private Double totalExpectedImprovement;

    // Confidence interval
    private Double effectConfidenceLower;
    private Double effectConfidenceUpper;

    // Evidence
    private EvidenceLevel evidenceLevel;
    private String evidenceLevelDescription;
    private String researchReferences;

    // Contraindications and recommendations
    private String contraindications;
    private String prerequisites;
    private String recommendedFor;

    // Simulation
    private String simulationModelId;
    private String simulationParameters;
    private Boolean isSimulatable;

    // Binaural beats
    private Double binauralFrequencyHz;
    private Integer binauralSessionMinutes;
    private String complementaryResources;
    private Boolean hasBinauralComponent;

    // Status
    private Boolean isActive;
    private ValidationStatus validationStatus;
    private String validationStatusDescription;
    private LocalDateTime lastValidated;
    private String createdBy;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Flags
    private Boolean isCrisisIntervention;
    private Boolean isHighIntensity;

    /**
     * Convert from Entity to DTO
     */
    public static InterventionDTO fromEntity(Intervention entity) {
        if (entity == null) return null;

        InterventionDTO dto = new InterventionDTO();

        dto.setId(entity.getId());
        dto.setInterventionCode(entity.getInterventionCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        // Type with display name
        dto.setInterventionType(entity.getInterventionType());
        if (entity.getInterventionType() != null) {
            dto.setInterventionTypeDisplayName(entity.getInterventionType().getDisplayName());
        }

        // Intensity with description
        dto.setIntensityLevel(entity.getIntensityLevel());
        if (entity.getIntensityLevel() != null) {
            dto.setIntensityLevelDescription(entity.getIntensityLevel().getDescription());
        }

        // Duration and frequency
        dto.setDurationWeeks(entity.getDurationWeeks());
        dto.setFrequencyPerWeek(entity.getFrequencyPerWeek());
        dto.setSessionDurationMinutes(entity.getSessionDurationMinutes());

        // Calculate total sessions
        if (entity.getDurationWeeks() != null && entity.getFrequencyPerWeek() != null) {
            dto.setTotalSessions(entity.getDurationWeeks() * entity.getFrequencyPerWeek());
        }

        // Expected effects
        dto.setExpectedStressReduction(entity.getExpectedStressReduction());
        dto.setExpectedDepressionReduction(entity.getExpectedDepressionReduction());
        dto.setExpectedAnxietyReduction(entity.getExpectedAnxietyReduction());
        dto.setExpectedResilienceIncrease(entity.getExpectedResilienceIncrease());
        dto.setTotalExpectedImprovement(entity.calculateTotalExpectedImprovement());

        // Confidence
        dto.setEffectConfidenceLower(entity.getEffectConfidenceLower());
        dto.setEffectConfidenceUpper(entity.getEffectConfidenceUpper());

        // Evidence
        dto.setEvidenceLevel(entity.getEvidenceLevel());
        if (entity.getEvidenceLevel() != null) {
            dto.setEvidenceLevelDescription(entity.getEvidenceLevel().getDescription());
        }
        dto.setResearchReferences(entity.getResearchReferences());

        // Contraindications
        dto.setContraindications(entity.getContraindications());
        dto.setPrerequisites(entity.getPrerequisites());
        dto.setRecommendedFor(entity.getRecommendedFor());

        // Simulation
        dto.setSimulationModelId(entity.getSimulationModelId());
        dto.setSimulationParameters(entity.getSimulationParameters());
        dto.setIsSimulatable(entity.getSimulationModelId() != null);

        // Binaural
        dto.setBinauralFrequencyHz(entity.getBinauralFrequencyHz());
        dto.setBinauralSessionMinutes(entity.getBinauralSessionMinutes());
        dto.setComplementaryResources(entity.getComplementaryResources());
        dto.setHasBinauralComponent(entity.getBinauralFrequencyHz() != null);

        // Status
        dto.setIsActive(entity.getIsActive());
        dto.setValidationStatus(entity.getValidationStatus());
        if (entity.getValidationStatus() != null) {
            dto.setValidationStatusDescription(entity.getValidationStatus().getDescription());
        }
        dto.setLastValidated(entity.getLastValidated());
        dto.setCreatedBy(entity.getCreatedBy());

        // Metadata
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Flags
        dto.setIsCrisisIntervention(entity.isSuitableForCrisis());
        dto.setIsHighIntensity(entity.isHighIntensity());

        return dto;
    }
}