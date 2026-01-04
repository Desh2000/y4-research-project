package com.research.mano.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Synthetic Data Record Entity for Component 1 (Privacy-Preserving Data Generation)
 * Stores synthetic mental health data generated for research and model training
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "synthetic_data_records")
public class SyntheticDataRecord extends BaseEntity {

    @Column(name = "record_id", unique = true, nullable = false)
    private String recordId; // Unique identifier for the synthetic record

    @Column(name = "source_user_cluster")
    private String sourceUserCluster; // Cluster of real user data used as a source

    @Column(name = "generation_method")
    @Enumerated(EnumType.STRING)
    private GenerationMethod generationMethod;

    // Synthetic mental health scores (0.0-1.0 range)
    @Column(name = "synthetic_stress_score")
    private Double syntheticStressScore;

    @Column(name = "synthetic_depression_score")
    private Double syntheticDepressionScore;

    @Column(name = "synthetic_anxiety_score")
    private Double syntheticAnxietyScore;

    @Column(name = "synthetic_resilience_score")
    private Double syntheticResilienceScore;

    // Demographic data (anonymized)
    @Column(name = "age_range")
    private String ageRange; // e.g., "25-30", "31-35"

    @Column(name = "gender_synthetic")
    @Enumerated(EnumType.STRING)
    private User.Gender genderSynthetic;

    @Column(name = "location_region")
    private String locationRegion; // General region, not specific location

    // Behavioral patterns (synthetic)
    @Column(name = "interaction_patterns", columnDefinition = "TEXT")
    private String interactionPatterns; // JSON of synthetic behavioral data

    @Column(name = "assessment_responses", columnDefinition = "TEXT")
    private String assessmentResponses; // JSON of synthetic assessment data

    @Column(name = "chat_interaction_synthetic", columnDefinition = "TEXT")
    private String chatInteractionSynthetic; // Synthetic chat patterns

    // Privacy and quality metrics
    @Column(name = "privacy_score")
    private Double privacyScore; // How well privacy is preserved (0.0-1.0)

    @Column(name = "utility_score")
    private Double utilityScore; // How useful the data is for research (0.0-1.0)

    @Column(name = "quality_metrics", columnDefinition = "TEXT")
    private String qualityMetrics; // JSON of various quality measures

    // Generation metadata
    @Column(name = "generation_timestamp")
    private LocalDateTime generationTimestamp;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "generation_parameters", columnDefinition = "TEXT")
    private String generationParameters; // JSON of parameters used

    @Column(name = "researcher_id")
    private String researcherId; // ID of the researcher who requested the data

    @Column(name = "research_purpose")
    private String researchPurpose;

    @Column(name = "is_validated")
    private Boolean isValidated = false;

    @Column(name = "validation_score")
    private Double validationScore;

    // Constructors
    public SyntheticDataRecord() {
        super();
        this.generationTimestamp = LocalDateTime.now();
    }

    public SyntheticDataRecord(String recordId, GenerationMethod method) {
        this();
        this.recordId = recordId;
        this.generationMethod = method;
    }

    // Getters and Setters

    @Override
    public String toString() {
        return "SyntheticDataRecord{" +
                "recordId='" + recordId + '\'' +
                ", method=" + generationMethod +
                ", privacyScore=" + privacyScore +
                ", utilityScore=" + utilityScore +
                '}';
    }

    /**
     * Generation Method Enum
     */
    public enum GenerationMethod {
        GAN("Generative Adversarial Network"),
        VAE("Variational Autoencoder"),
        DIFFERENTIAL_PRIVACY("Differential Privacy Method"),
        SYNTHETIC_MINORITY("SMOTE - Synthetic Minority Oversampling"),
        BAYESIAN("Bayesian Network Generation"),
        DEEP_LEARNING("Deep Learning Based Generation");

        private final String description;

        GenerationMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}