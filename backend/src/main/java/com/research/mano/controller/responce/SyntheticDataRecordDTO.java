package com.research.mano.controller.responce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyntheticDataRecordDTO {
    private Long id;
    private String recordId;
    private String sourceUserCluster;
    private String generationMethod;
    private String researcherId;
    private String researchPurpose;
    private String modelVersion;
    private Double privacyScore;
    private Double utilityScore;
    private Boolean isValidated;
    private Double validationScore;
    private LocalDateTime generationTimestamp;

    // Synthetic scores
    private Double syntheticStressScore;
    private Double syntheticDepressionScore;
    private Double syntheticAnxietyScore;
    private Double syntheticResilienceScore;

    // Anonymized demographics
    private String ageRange;
    private String genderSynthetic;
    private String locationRegion;

    // Behavioral patterns (JSON strings)
    private String interactionPatterns;
    private String assessmentResponses;
    private String chatInteractionSynthetic;
}
