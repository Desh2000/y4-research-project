package com.reserch.mano.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private Integer stressLevel;
    private Integer anxietyLevel;
    private Integer moodRating;
    private Integer sleepQuality;
    private Integer activityLevel;
    private Double resilienceScore;
    private Double riskScore;
    private Boolean privacyConsent;
    private Boolean dataSharingConsent;
    private String notificationPreferences;
    private LocalDateTime updatedAt;
}
