package com.research.mano.controller.request;

import com.research.mano.dto.ml.PredictionInput;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for mental health predictions
 * Supports both simple questionnaire-based and full multimodal predictions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    // ==================== SIMPLE PREDICTION (Questionnaire Only) ====================

    /**
     * PHQ-9 Depression Questionnaire responses (9 items, 0-3 each)
     */
    @Size(min = 9, max = 9, message = "PHQ-9 requires exactly 9 responses")
    private List<@Min(0) @Max(3) Integer> phq9Responses;

    /**
     * GAD-7 Anxiety Questionnaire responses (7 items, 0-3 each)
     */
    @Size(min = 7, max = 7, message = "GAD-7 requires exactly 7 responses")
    private List<@Min(0) @Max(3) Integer> gad7Responses;

    /**
     * PSS Perceived Stress Scale responses (10 items, 0-4 each)
     */
    @Size(min = 10, max = 10, message = "PSS requires exactly 10 responses")
    private List<@Min(0) @Max(4) Integer> pssResponses;

    // ==================== DIRECT SCORE INPUT (Manual/External) ====================

    /**
     * Direct stress score input (0.0-1.0)
     */
    @DecimalMin(value = "0.0", message = "Stress score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Stress score must be <= 1.0")
    private Double stressScore;

    /**
     * Direct depression score input (0.0-1.0)
     */
    @DecimalMin(value = "0.0", message = "Depression score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Depression score must be <= 1.0")
    private Double depressionScore;

    /**
     * Direct anxiety score input (0.0-1.0)
     */
    @DecimalMin(value = "0.0", message = "Anxiety score must be >= 0.0")
    @DecimalMax(value = "1.0", message = "Anxiety score must be <= 1.0")
    private Double anxietyScore;

    // ==================== WEARABLE DATA ====================

    /**
     * Sleep data from wearables
     */
    private List<SleepDataEntry> sleepData;

    /**
     * Activity data from wearables
     */
    private List<ActivityDataEntry> activityData;

    /**
     * Heart rate variability data
     */
    private List<HRVDataEntry> hrvData;

    // ==================== TEXT DATA ====================

    /**
     * Recent journal entries for sentiment analysis
     */
    private List<String> journalEntries;

    /**
     * Recent chat messages for analysis
     */
    private List<String> recentMessages;

    // ==================== DEMOGRAPHICS ====================

    private Integer age;
    private String gender;
    private String occupation;
    private Boolean hasPriorMentalHealthHistory;
    private Boolean currentlyInTherapy;
    private Boolean onMedication;

    // ==================== OPTIONS ====================

    /**
     * Model version to use (optional)
     */
    private String modelVersion;

    /**
     * Data source identifier
     */
    private String dataSource;

    /**
     * Whether to include future predictions
     */
    private Boolean includeFuturePredictions = false;

    /**
     * Whether to include feature importance
     */
    private Boolean includeFeatureImportance = false;

    // ==================== NESTED CLASSES ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SleepDataEntry {
        private String date;

        @DecimalMin(value = "0.0", message = "Duration must be >= 0")
        @DecimalMax(value = "24.0", message = "Duration must be <= 24 hours")
        private Double durationHours;

        @DecimalMin(value = "0.0", message = "Quality must be >= 0")
        @DecimalMax(value = "100.0", message = "Quality must be <= 100")
        private Double qualityScore;

        private Double deepSleepPercentage;
        private Integer awakenings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDataEntry {
        private String date;

        @Min(value = 0, message = "Steps cannot be negative")
        private Integer steps;

        @Min(value = 0, message = "Active minutes cannot be negative")
        private Integer activeMinutes;

        private Integer calories;
        private String activityType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HRVDataEntry {
        private String date;

        @DecimalMin(value = "0.0", message = "HRV must be >= 0")
        private Double hrvMs;

        @Min(value = 30, message = "Heart rate must be >= 30")
        @Max(value = 220, message = "Heart rate must be <= 220")
        private Integer restingHeartRate;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if this is a simple questionnaire-only request
     */
    public boolean isQuestionnaireOnly() {
        return (phq9Responses != null || gad7Responses != null || pssResponses != null) &&
                sleepData == null && activityData == null && hrvData == null;
    }

    /**
     * Check if direct scores are provided
     */
    public boolean hasDirectScores() {
        return stressScore != null || depressionScore != null || anxietyScore != null;
    }

    /**
     * Check if wearable data is included
     */
    public boolean hasWearableData() {
        return (sleepData != null && !sleepData.isEmpty()) ||
                (activityData != null && !activityData.isEmpty()) ||
                (hrvData != null && !hrvData.isEmpty());
    }

    /**
     * Convert to ML service input format
     */
    public PredictionInput toPredictionInput(Long userId) {
        PredictionInput input = new PredictionInput();
        input.setUserId(userId);
        input.setTimestamp(java.time.LocalDateTime.now().toString());
        input.setModelVersion(modelVersion);

        // Questionnaire data
        input.setPhq9Responses(phq9Responses);
        input.setGad7Responses(gad7Responses);
        input.setPssResponses(pssResponses);

        // Convert sleep data
        if (sleepData != null) {
            input.setSleepHistory(sleepData.stream()
                    .map(s -> PredictionInput.SleepEntry.builder()
                            .date(s.getDate())
                            .durationHours(s.getDurationHours())
                            .qualityScore(s.getQualityScore())
                            .deepSleepPercentage(s.getDeepSleepPercentage())
                            .awakenings(s.getAwakenings())
                            .build())
                    .toList());
        }

        // Convert activity data
        if (activityData != null) {
            input.setActivityHistory(activityData.stream()
                    .map(a -> PredictionInput.ActivityEntry.builder()
                            .date(a.getDate())
                            .steps(a.getSteps())
                            .activeMinutes(a.getActiveMinutes())
                            .calories(a.getCalories())
                            .activityType(a.getActivityType())
                            .build())
                    .toList());
        }

        // Convert HRV data
        if (hrvData != null) {
            input.setHrvHistory(hrvData.stream()
                    .map(h -> PredictionInput.HRVEntry.builder()
                            .date(h.getDate())
                            .hrvMs(h.getHrvMs())
                            .restingHeartRate(h.getRestingHeartRate())
                            .build())
                    .toList());
        }

        // Demographics
        if (age != null || gender != null) {
            input.setDemographics(PredictionInput.Demographics.builder()
                    .age(age)
                    .gender(gender)
                    .occupation(occupation)
                    .hasPriorMentalHealthHistory(hasPriorMentalHealthHistory)
                    .currentlyInTherapy(currentlyInTherapy)
                    .onMedication(onMedication)
                    .build());
        }

        // Text data
        if (journalEntries != null) {
            input.setRecentTextEntries(journalEntries);
        } else if (recentMessages != null) {
            input.setRecentTextEntries(recentMessages);
        }

        return input;
    }
}