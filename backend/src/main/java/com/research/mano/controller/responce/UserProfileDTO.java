package com.research.mano.controller.responce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private Long userId;
    private String bio;
    private String location;
    private String timezone;
    private String preferredLanguage;

    // Mental health scores
    private Double currentStressScore;
    private Double currentAnxietyScore;
    private Double currentDepressionScore;
    private Double resilienceScore;
    private LocalDateTime lastAssessmentDate;

    // Therapy information
    private LocalDateTime therapyStartDate;
    private String currentMedications;
    private String mentalHealthGoals;
    private String crisisInterventionPlan;
    private Integer supportNetworkSize;

    // Cluster information
    private String currentClusterIdentifier;
    private LocalDateTime clusterAssignmentDate;
    private String previousClusterIdentifier;
    private Double clusterStabilityScore;

    // Risk flags
    private Boolean highRiskAlert;
    private LocalDateTime lastHighRiskDate;
    private Boolean interventionRequired;

    // Settings
    private Boolean syntheticDataOptIn;
    private Boolean predictionAlertsEnabled;
    private Integer chatbotInteractionCount;
}