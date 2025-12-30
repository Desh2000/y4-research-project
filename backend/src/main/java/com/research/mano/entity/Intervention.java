package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Intervention Entity for Component 1 (AMISE - Adaptive Multimodal Intervention Simulation Engine)
 * Represents mental health interventions that can be simulated and tracked.
 *
 * This entity supports:
 * - Intervention type categorization (CBT, Medication, Exercise, etc.)
 * - Intensity levels (Low, Medium, High)
 * - Duration tracking
 * - Expected and actual outcomes
 * - Integration with synthetic data simulation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "interventions")
public class Intervention extends BaseEntity {

    @Column(name = "intervention_code", unique = true, nullable = false)
    private String interventionCode; // e.g., "CBT_HIGH_8W", "MED_SSRI_LOW"

    @Column(name = "name", nullable = false)
    private String name; // e.g., "Cognitive Behavioral Therapy"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "intervention_type", nullable = false)
    private InterventionType interventionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "intensity_level", nullable = false)
    private IntensityLevel intensityLevel;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "frequency_per_week")
    private Integer frequencyPerWeek; // Sessions per week

    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes;

    // Expected effects on mental health scores (changes in 0.0-1.0 range)
    @Column(name = "expected_stress_reduction")
    private Double expectedStressReduction; // e.g., 0.15 means 15% reduction

    @Column(name = "expected_depression_reduction")
    private Double expectedDepressionReduction;

    @Column(name = "expected_anxiety_reduction")
    private Double expectedAnxietyReduction;

    @Column(name = "expected_resilience_increase")
    private Double expectedResilienceIncrease;

    // Confidence intervals for expected effects
    @Column(name = "effect_confidence_lower")
    private Double effectConfidenceLower; // Lower bound of confidence interval

    @Column(name = "effect_confidence_upper")
    private Double effectConfidenceUpper; // Upper bound of confidence interval

    // Evidence and research basis
    @Column(name = "evidence_level")
    @Enumerated(EnumType.STRING)
    private EvidenceLevel evidenceLevel;

    @Column(name = "research_references", columnDefinition = "TEXT")
    private String researchReferences; // JSON array of research paper references

    // Contraindications and requirements
    @Column(name = "contraindications", columnDefinition = "TEXT")
    private String contraindications; // JSON array of conditions where this intervention shouldn't be used

    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites; // JSON array of requirements before starting

    @Column(name = "recommended_for", columnDefinition = "TEXT")
    private String recommendedFor; // JSON array of conditions this is best suited for

    // Simulation parameters for Component 1
    @Column(name = "simulation_model_id")
    private String simulationModelId; // ID of the ML model used to simulate this intervention

    @Column(name = "simulation_parameters", columnDefinition = "TEXT")
    private String simulationParameters; // JSON of parameters for simulation

    // Status and metadata
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private String createdBy; // Researcher who added this intervention

    @Column(name = "last_validated")
    private LocalDateTime lastValidated;

    @Column(name = "validation_status")
    @Enumerated(EnumType.STRING)
    private ValidationStatus validationStatus;

    // Binaural beats integration (as per Component 1 requirements)
    @Column(name = "binaural_frequency_hz")
    private Double binauralFrequencyHz; // e.g., 10.0 for alpha waves

    @Column(name = "binaural_session_minutes")
    private Integer binauralSessionMinutes;

    @Column(name = "complementary_resources", columnDefinition = "TEXT")
    private String complementaryResources; // JSON array of YouTube/Spotify links

    // Constructors for common use cases
    public Intervention(String name, InterventionType type, IntensityLevel intensity) {
        this.name = name;
        this.interventionType = type;
        this.intensityLevel = intensity;
        this.interventionCode = generateCode(type, intensity);
        this.isActive = true;
        this.validationStatus = ValidationStatus.PENDING;
    }

    // Utility methods
    private String generateCode(InterventionType type, IntensityLevel intensity) {
        return type.name() + "_" + intensity.name() + "_" + System.currentTimeMillis() % 10000;
    }

    public Double calculateTotalExpectedImprovement() {
        double stress = expectedStressReduction != null ? expectedStressReduction : 0.0;
        double depression = expectedDepressionReduction != null ? expectedDepressionReduction : 0.0;
        double anxiety = expectedAnxietyReduction != null ? expectedAnxietyReduction : 0.0;
        return (stress + depression + anxiety) / 3.0;
    }

    public boolean isHighIntensity() {
        return intensityLevel == IntensityLevel.HIGH;
    }

    public boolean isSuitableForCrisis() {
        return interventionType == InterventionType.CRISIS_SUPPORT ||
                interventionType == InterventionType.EMERGENCY_HOTLINE;
    }

    @Override
    public String toString() {
        return "Intervention{" +
                "code='" + interventionCode + '\'' +
                ", name='" + name + '\'' +
                ", type=" + interventionType +
                ", intensity=" + intensityLevel +
                ", durationWeeks=" + durationWeeks +
                '}';
    }

    // ==================== ENUMS ====================

    /**
     * Types of mental health interventions
     */
    public enum InterventionType {
        // Therapy-based interventions
        CBT("Cognitive Behavioral Therapy"),
        DBT("Dialectical Behavior Therapy"),
        PSYCHOTHERAPY("General Psychotherapy"),
        GROUP_THERAPY("Group Therapy Sessions"),

        // Medical interventions
        MEDICATION_SSRI("SSRI Medication"),
        MEDICATION_SNRI("SNRI Medication"),
        MEDICATION_OTHER("Other Medication"),

        // Lifestyle interventions
        EXERCISE_AEROBIC("Aerobic Exercise"),
        EXERCISE_YOGA("Yoga Practice"),
        EXERCISE_WALKING("Walking/Outdoor Activity"),

        // Mindfulness and relaxation
        MINDFULNESS("Mindfulness Meditation"),
        BREATHING_EXERCISES("Breathing Exercises"),
        PROGRESSIVE_RELAXATION("Progressive Muscle Relaxation"),
        BINAURAL_BEATS("Binaural Beats Therapy"),

        // Sleep-related
        SLEEP_HYGIENE("Sleep Hygiene Program"),
        SLEEP_CBT("CBT for Insomnia"),

        // Social support
        PEER_SUPPORT("Peer Support Group"),
        FAMILY_THERAPY("Family Therapy"),
        SOCIAL_SKILLS("Social Skills Training"),

        // Crisis interventions
        CRISIS_SUPPORT("Crisis Intervention"),
        EMERGENCY_HOTLINE("Emergency Hotline Contact"),

        // Digital interventions
        APP_BASED("App-Based Intervention"),
        CHATBOT_SUPPORT("AI Chatbot Support"),

        // Combined/Multimodal
        MULTIMODAL("Multimodal Intervention");

        private final String displayName;

        InterventionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Intensity levels for interventions
     */
    public enum IntensityLevel {
        MINIMAL("Minimal - Self-guided, low frequency"),
        LOW("Low - Weekly sessions, light commitment"),
        MEDIUM("Medium - 2-3 sessions/week, moderate commitment"),
        HIGH("High - Daily or intensive program"),
        INTENSIVE("Intensive - Multiple daily sessions, residential");

        private final String description;

        IntensityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Evidence levels for intervention effectiveness
     */
    public enum EvidenceLevel {
        STRONG("Strong - Multiple RCTs, meta-analyses"),
        MODERATE("Moderate - Some RCTs, consistent findings"),
        LIMITED("Limited - Few studies, mixed results"),
        EMERGING("Emerging - New research, promising"),
        ANECDOTAL("Anecdotal - Based on clinical experience");

        private final String description;

        EvidenceLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Validation status for interventions
     */
    public enum ValidationStatus {
        PENDING("Pending validation"),
        VALIDATED("Validated by research team"),
        REJECTED("Rejected - insufficient evidence"),
        NEEDS_UPDATE("Needs update based on new research");

        private final String description;

        ValidationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}