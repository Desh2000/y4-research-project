package com.research.mano.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * General Helper Utilities
 * Provides common utility methods for the mental health application
 */
@Component
public class GeneralHelper {

    private static final Random random = new Random();

    /**
     * Generate unique identifier with prefix
     */
    public String generateUniqueId(String prefix) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "-" + timestamp + "-" + uuid;
    }

    /**
     * Generate session ID for chat
     */
    public String generateChatSessionId(Long userId) {
        return "SESSION-" + userId + "-" + System.currentTimeMillis() +
                "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate record ID for synthetic data
     */
    public String generateSyntheticRecordId() {
        return generateUniqueId("SYN");
    }

    /**
     * Calculate overall risk score from individual scores
     */
    public Double calculateOverallRisk(Double stressScore, Double depressionScore, Double anxietyScore) {
        if (stressScore == null || depressionScore == null || anxietyScore == null) {
            return null;
        }

        // Weighted average: stress 40%, depression 35%, anxiety 25%
        return (stressScore * 0.4) + (depressionScore * 0.35) + (anxietyScore * 0.25);
    }

    /**
     * Calculate resilience score (inverse of average risk)
     */
    public Double calculateResilienceScore(Double stressScore, Double depressionScore, Double anxietyScore) {
        Double overallRisk = calculateOverallRisk(stressScore, depressionScore, anxietyScore);
        if (overallRisk == null) return null;

        return Math.max(0.0, 1.0 - overallRisk);
    }

    /**
     * Determine cluster category based on dominant score
     */
    public String determineClusterCategory(Double stressScore, Double depressionScore, Double anxietyScore) {
        if (stressScore == null || depressionScore == null || anxietyScore == null) {
            return "MIXED";
        }

        Double maxScore = Collections.max(Arrays.asList(stressScore, depressionScore, anxietyScore));

        if (maxScore.equals(stressScore)) {
            return "STRESS";
        } else if (maxScore.equals(depressionScore)) {
            return "DEPRESSION";
        } else {
            return "ANXIETY";
        }
    }

    /**
     * Determine cluster level based on score intensity
     */
    public String determineClusterLevel(Double score) {
        if (score == null) return "LOW";

        if (score >= 0.8) {
            return "HIGH";
        } else if (score >= 0.4) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Generate cluster identifier
     */
    public String generateClusterIdentifier(Double stressScore, Double depressionScore, Double anxietyScore) {
        String category = determineClusterCategory(stressScore, depressionScore, anxietyScore);
        Double dominantScore = Collections.max(Arrays.asList(stressScore, depressionScore, anxietyScore));
        String level = determineClusterLevel(dominantScore);

        return category + "_" + level;
    }

    /**
     * Check if scores indicate high risk
     */
    public boolean isHighRisk(Double stressScore, Double depressionScore, Double anxietyScore) {
        return (stressScore != null && stressScore >= 0.8) ||
                (depressionScore != null && depressionScore >= 0.8) ||
                (anxietyScore != null && anxietyScore >= 0.8);
    }

    /**
     * Format timestamp for display
     */
    public String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Format timestamp for file names
     */
    public String formatTimestampForFileName(LocalDateTime timestamp) {
        if (timestamp == null) timestamp = LocalDateTime.now();
        return timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * Mask sensitive data (email, phone)
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) return email;

        String maskedUsername = username.substring(0, 2) + "*".repeat(username.length() - 2);
        return maskedUsername + "@" + domain;
    }

    /**
     * Mask phone number
     */
    public String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) return phone;

        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }

    /**
     * Calculate percentage change between two values
     */
    public Double calculatePercentageChange(Double oldValue, Double newValue) {
        if (oldValue == null || newValue == null || oldValue == 0.0) return null;

        return ((newValue - oldValue) / oldValue) * 100.0;
    }

    /**
     * Normalize score to 0-1 range
     */
    public Double normalizeScore(Double score, Double minValue, Double maxValue) {
        if (score == null || minValue == null || maxValue == null || minValue.equals(maxValue)) {
            return null;
        }

        return (score - minValue) / (maxValue - minValue);
    }

    /**
     * Calculate weighted average of scores
     */
    public Double calculateWeightedAverage(Map<Double, Double> scoreWeightMap) {
        if (scoreWeightMap == null || scoreWeightMap.isEmpty()) return null;

        double totalWeight = scoreWeightMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight == 0.0) return null;

        double weightedSum = scoreWeightMap.entrySet().stream()
                .mapToDouble(entry -> entry.getKey() * entry.getValue())
                .sum();

        return weightedSum / totalWeight;
    }

    /**
     * Generate random score within range (for synthetic data)
     */
    public Double generateRandomScore(Double min, Double max) {
        if (min == null || max == null || min >= max) return 0.5;

        return min + (random.nextDouble() * (max - min));
    }

    /**
     * Add noise to score (for synthetic data privacy)
     */
    public Double addNoiseToScore(Double score, Double noiseLevel) {
        if (score == null || noiseLevel == null) return score;

        double noise = (random.nextGaussian() * noiseLevel);
        double noisyScore = score + noise;

        // Clamp to valid range
        return Math.max(0.0, Math.min(1.0, noisyScore));
    }

    /**
     * Calculate privacy score based on anonymization level
     */
    public Double calculatePrivacyScore(String anonymizationMethod, boolean hasNoise, boolean isGeneralized) {
        double baseScore = 0.6;

        // Method contribution
        switch (anonymizationMethod.toUpperCase()) {
            case "DIFFERENTIAL_PRIVACY" -> baseScore += 0.3;
            case "K_ANONYMITY" -> baseScore += 0.2;
            case "L_DIVERSITY" -> baseScore += 0.25;
            case "T_CLOSENESS" -> baseScore += 0.2;
            default -> baseScore += 0.1;
        }

        // Additional privacy measures
        if (hasNoise) baseScore += 0.05;
        if (isGeneralized) baseScore += 0.05;

        return Math.min(1.0, baseScore);
    }

    /**
     * Calculate utility score based on data completeness and accuracy
     */
    public Double calculateUtilityScore(int completedFields, int totalFields, Double accuracyScore) {
        if (totalFields == 0) return 0.0;

        double completenessScore = (double) completedFields / totalFields;
        double finalAccuracyScore = accuracyScore != null ? accuracyScore : 0.8;

        // Weighted average: completeness 60%, accuracy 40%
        return (completenessScore * 0.6) + (finalAccuracyScore * 0.4);
    }

    /**
     * Truncate text to specified length
     */
    public String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;

        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Convert list to comma-separated string
     */
    public String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";

        return String.join(", ", list);
    }

    /**
     * Convert comma-separated string to list
     */
    public List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) return new ArrayList<>();

        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Generate age range string
     */
    public String generateAgeRange(Integer age) {
        if (age == null) return "Unknown";

        if (age < 18) return "Under 18";
        if (age < 25) return "18-24";
        if (age < 35) return "25-34";
        if (age < 45) return "35-44";
        if (age < 55) return "45-54";
        if (age < 65) return "55-64";
        return "65+";
    }

    /**
     * Check if timestamp is within business hours
     */
    public boolean isWithinBusinessHours(LocalDateTime timestamp) {
        if (timestamp == null) return false;

        int hour = timestamp.getHour();
        int dayOfWeek = timestamp.getDayOfWeek().getValue();

        // Monday to Friday, 9 AM to 5 PM
        return dayOfWeek >= 1 && dayOfWeek <= 5 && hour >= 9 && hour < 17;
    }

    /**
     * Calculate time difference in hours
     */
    public long calculateHoursDifference(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;

        return java.time.Duration.between(start, end).toHours();
    }

    /**
     * Check if date is within last N days
     */
    public boolean isWithinLastNDays(LocalDateTime date, int days) {
        if (date == null) return false;

        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return date.isAfter(cutoff);
    }

    /**
     * Generate summary statistics for a list of scores
     */
    public Map<String, Double> generateScoreStatistics(List<Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return Map.of(
                    "count", 0.0,
                    "min", 0.0,
                    "max", 0.0,
                    "average", 0.0,
                    "median", 0.0
            );
        }

        List<Double> sortedScores = scores.stream().sorted().collect(Collectors.toList());

        double min = sortedScores.get(0);
        double max = sortedScores.get(sortedScores.size() - 1);
        double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double median;
        int size = sortedScores.size();
        if (size % 2 == 0) {
            median = (sortedScores.get(size / 2 - 1) + sortedScores.get(size / 2)) / 2.0;
        } else {
            median = sortedScores.get(size / 2);
        }

        return Map.of(
                "count", (double) scores.size(),
                "min", min,
                "max", max,
                "average", average,
                "median", median
        );
    }

    /**
     * Check if system is in maintenance mode
     */
    public boolean isSystemInMaintenance() {
        // This could be configured via properties or database
        // For now, return false
        return false;
    }

    /**
     * Generate alert priority based on severity and user risk
     */
    public String calculateAlertPriority(String severityLevel, boolean isHighRiskUser, boolean isCrisis) {
        if (isCrisis) return "CRITICAL";

        switch (severityLevel.toUpperCase()) {
            case "CRITICAL" -> {
                return "CRITICAL";
            }
            case "HIGH" -> {
                return isHighRiskUser ? "CRITICAL" : "HIGH";
            }
            case "MEDIUM" -> {
                return isHighRiskUser ? "HIGH" : "MEDIUM";
            }
            default -> {
                return "LOW";
            }
        }
    }
}