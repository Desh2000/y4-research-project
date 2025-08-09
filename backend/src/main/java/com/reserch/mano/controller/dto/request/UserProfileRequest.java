package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @Min(value = 1, message = "Stress level must be between 1 and 10")
    @Max(value = 10, message = "Stress level must be between 1 and 10")
    private Integer stressLevel;

    @Min(value = 1, message = "Anxiety level must be between 1 and 10")
    @Max(value = 10, message = "Anxiety level must be between 1 and 10")
    private Integer anxietyLevel;

    @Min(value = 1, message = "Mood rating must be between 1 and 10")
    @Max(value = 10, message = "Mood rating must be between 1 and 10")
    private Integer moodRating;

    @Min(value = 1, message = "Sleep quality must be between 1 and 10")
    @Max(value = 10, message = "Sleep quality must be between 1 and 10")
    private Integer sleepQuality;

    @Min(value = 1, message = "Activity level must be between 1 and 10")
    @Max(value = 10, message = "Activity level must be between 1 and 10")
    private Integer activityLevel;

    private Boolean privacyConsent;
    private Boolean dataSharingConsent;
    private String notificationPreferences;
}
