package com.research.mano.dto.ml;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Input DTO for LSTM Risk Prediction Model (Component 2)
 * Represents the multimodal data sent to the Python ML service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionInput {

    /**
     * User identifier
     */
    private Long userId;

    /**
     * Request timestamp
     */
    private String timestamp;

    /**
     * Model version to use (optional, uses latest if not specified)
     */
    private String modelVersion;

    // ==================== TEMPORAL FEATURES (Time-series data) ====================

    /**
     * Historical mood scores (last N days)
     * Each entry: {date, mood_score (1-10)}
     */
    private List<TimeSeriesEntry> moodHistory;

    /**
     * Sleep data from wearables
     * Each entry: {date, duration_hours, quality_score, deep_sleep_pct}
     */
    private List<SleepEntry> sleepHistory;

    /**
     * Activity/Exercise data
     * Each entry: {date, steps, active_minutes, calories}
     */
    private List<ActivityEntry> activityHistory;

    /**
     * Heart rate variability data
     * Each entry: {date, hrv_ms, resting_hr}
     */
    private List<HRVEntry> hrvHistory;

    /**
     * Previous assessment scores
     * Each entry: {date, stress, depression, anxiety}
     */
    private List<AssessmentEntry> assessmentHistory;

    // ==================== STATIC FEATURES (Demographics) ====================

    /**
     * Demographic information
     */
    private Demographics demographics;

    // ==================== QUESTIONNAIRE DATA ====================

    /**
     * PHQ-9 Depression Questionnaire responses (0-3 per item)
     */
    private List<Integer> phq9Responses;

    /**
     * GAD-7 Anxiety Questionnaire responses (0-3 per item)
     */
    private List<Integer> gad7Responses;

    /**
     * PSS Perceived Stress Scale responses (0-4 per item)
     */
    private List<Integer> pssResponses;

    /**
     * Custom questionnaire responses
     */
    private Map<String, Object> customResponses;

    // ==================== TEXT DATA (for NLP features) ====================

    /**
     * Recent journal entries or chat messages for sentiment analysis
     */
    private List<String> recentTextEntries;

    public String getRequestId() {
        return "";
    }

    // ==================== NESTED CLASSES ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesEntry {
        private String date;
        private Double value;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SleepEntry {
        private String date;
        private Double durationHours;
        private Double qualityScore; // 0-100
        private Double deepSleepPercentage;
        private Double remSleepPercentage;
        private Integer awakenings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityEntry {
        private String date;
        private Integer steps;
        private Integer activeMinutes;
        private Integer calories;
        private Double distanceKm;
        private String activityType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HRVEntry {
        private String date;
        private Double hrvMs; // Heart rate variability in milliseconds
        private Integer restingHeartRate;
        private Integer maxHeartRate;
        private Double stressLevel; // Derived from HRV
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentEntry {
        private String date;
        private Double stressScore;
        private Double depressionScore;
        private Double anxietyScore;
        private String assessmentType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Demographics {
        private Integer age;
        private String gender;
        private String occupation;
        private String educationLevel;
        private String maritalStatus;
        private Boolean hasChildren;
        private String employmentStatus;
        private Double incomeLevel; // Normalized 0-1
        private String livingArrangement;
        private Boolean hasPriorMentalHealthHistory;
        private Boolean currentlyInTherapy;
        private Boolean onMedication;
        private Integer supportNetworkSize;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Calculate PHQ-9 total score
     */
    public Integer calculatePHQ9Score() {
        if (phq9Responses == null || phq9Responses.isEmpty()) return null;
        return phq9Responses.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Calculate GAD-7 total score
     */
    public Integer calculateGAD7Score() {
        if (gad7Responses == null || gad7Responses.isEmpty()) return null;
        return gad7Responses.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Calculate PSS total score
     */
    public Integer calculatePSSScore() {
        if (pssResponses == null || pssResponses.isEmpty()) return null;
        return pssResponses.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Get average sleep quality from history
     */
    public Double getAverageSleepQuality() {
        if (sleepHistory == null || sleepHistory.isEmpty()) return null;
        return sleepHistory.stream()
                .filter(s -> s.getQualityScore() != null)
                .mapToDouble(SleepEntry::getQualityScore)
                .average()
                .orElse(0.0);
    }

    /**
     * Get average daily steps from history
     */
    public Double getAverageDailySteps() {
        if (activityHistory == null || activityHistory.isEmpty()) return null;
        return activityHistory.stream()
                .filter(a -> a.getSteps() != null)
                .mapToInt(ActivityEntry::getSteps)
                .average()
                .orElse(0.0);
    }
}