package com.research.mano.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Validation Helper Utilities
 * Provides common validation methods for the mental health application
 */
@Component
public class ValidationHelper {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Phone validation pattern (basic international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[1-9]\\d{1,14}$"
    );

    // Username validation pattern (alphanumeric, underscore, hyphen)
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_-]{3,20}$"
    );

    // Crisis keywords for detection
    private static final String[] CRISIS_KEYWORDS = {
            "suicide", "kill myself", "end it all", "can't go on", "want to die",
            "hurt myself", "self-harm", "hopeless", "worthless", "give up",
            "end my life", "no point", "better off dead", "harm myself"
    };

    /**
     * Validate mental health scores (must be between 0.0 and 1.0)
     */
    public boolean isValidMentalHealthScore(Double score) {
        return score != null && score >= 0.0 && score <= 1.0;
    }

    /**
     * Validate all three mental health scores
     */
    public boolean areValidMentalHealthScores(Double stressScore, Double depressionScore, Double anxietyScore) {
        return isValidMentalHealthScore(stressScore) &&
                isValidMentalHealthScore(depressionScore) &&
                isValidMentalHealthScore(anxietyScore);
    }

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate username format
     */
    public boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password strength
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        // Check for at least one digit, one lowercase, one uppercase
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);

        return hasDigit && hasLower && hasUpper;
    }

    /**
     * Validate age based on date of birth
     */
    public boolean isValidAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return false;

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= 13 && age <= 120; // Minimum age 13, maximum 120
    }

    /**
     * Check if text contains crisis keywords
     */
    public boolean containsCrisisKeywords(String text) {
        if (text == null) return false;

        String lowerText = text.toLowerCase();
        for (String keyword : CRISIS_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate sentiment score (-1.0 to 1.0)
     */
    public boolean isValidSentimentScore(Double score) {
        return score != null && score >= -1.0 && score <= 1.0;
    }

    /**
     * Validate privacy/utility scores (0.0 to 1.0)
     */
    public boolean isValidPrivacyUtilityScore(Double score) {
        return score != null && score >= 0.0 && score <= 1.0;
    }

    /**
     * Validate date range
     */
    public boolean isValidDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }

    /**
     * Validate cluster identifier format
     */
    public boolean isValidClusterIdentifier(String clusterIdentifier) {
        if (clusterIdentifier == null) return false;

        // Format: CATEGORY_LEVEL (e.g., STRESS_HIGH, DEPRESSION_MEDIUM)
        String[] parts = clusterIdentifier.split("_");
        if (parts.length != 2) return false;

        try {
            // Validate category
            com.research.mano.entity.MentalHealthPrediction.ClusterCategory.valueOf(parts[0]);
            // Validate level
            com.research.mano.entity.MentalHealthPrediction.ClusterLevel.valueOf(parts[1]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validate research purpose length and content
     */
    public boolean isValidResearchPurpose(String purpose) {
        return purpose != null &&
                purpose.trim().length() >= 10 &&
                purpose.trim().length() <= 500;
    }

    /**
     * Validate support network size
     */
    public boolean isValidSupportNetworkSize(Integer size) {
        return size != null && size >= 0 && size <= 100;
    }

    /**
     * Validate timezone format
     */
    public boolean isValidTimezone(String timezone) {
        if (timezone == null) return true; // Optional field

        try {
            java.time.ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate language code (ISO 639-1)
     */
    public boolean isValidLanguageCode(String languageCode) {
        if (languageCode == null) return true; // Optional field

        return languageCode.matches("^[a-z]{2}(-[A-Z]{2})?$");
    }

    /**
     * Check if score indicates high risk (>= 0.8)
     */
    public boolean isHighRiskScore(Double score) {
        return score != null && score >= 0.8;
    }

    /**
     * Check if any mental health scores indicate high risk
     */
    public boolean isHighRiskProfile(Double stressScore, Double depressionScore, Double anxietyScore) {
        return isHighRiskScore(stressScore) ||
                isHighRiskScore(depressionScore) ||
                isHighRiskScore(anxietyScore);
    }

    /**
     * Validate response time (should be reasonable)
     */
    public boolean isValidResponseTime(Long responseTimeMs) {
        return responseTimeMs != null &&
                responseTimeMs >= 0 &&
                responseTimeMs <= 30000; // Max 30 seconds
    }

    /**
     * Validate batch size for synthetic data generation
     */
    public boolean isValidBatchSize(Integer batchSize) {
        return batchSize != null && batchSize >= 1 && batchSize <= 1000;
    }

    /**
     * Validate model version format
     */
    public boolean isValidModelVersion(String modelVersion) {
        if (modelVersion == null) return true; // Optional field

        // Format: x.y.z (semantic versioning)
        return modelVersion.matches("^\\d+\\.\\d+(\\.\\d+)?$");
    }

    /**
     * Validate session ID format
     */
    public boolean isValidSessionId(String sessionId) {
        return sessionId != null &&
                sessionId.startsWith("SESSION-") &&
                sessionId.length() > 10;
    }

    /**
     * Validate record ID format for synthetic data
     */
    public boolean isValidSyntheticRecordId(String recordId) {
        return recordId != null &&
                (recordId.startsWith("SYN-") || recordId.startsWith("SYNTHETIC-")) &&
                recordId.length() > 10;
    }

    /**
     * Validate alert severity escalation
     */
    public boolean isValidSeverityEscalation(String currentSeverity, String newSeverity) {
        try {
            com.research.mano.entity.SystemAlert.SeverityLevel current =
                    com.research.mano.entity.SystemAlert.SeverityLevel.valueOf(currentSeverity);
            com.research.mano.entity.SystemAlert.SeverityLevel newLevel =
                    com.research.mano.entity.SystemAlert.SeverityLevel.valueOf(newSeverity);

            // Can only escalate to higher severity
            return newLevel.ordinal() >= current.ordinal();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get validation error message for mental health scores
     */
    public String getMentalHealthScoreValidationMessage(String scoreType, Double score) {
        if (score == null) {
            return scoreType + " score is required";
        }
        if (score < 0.0 || score > 1.0) {
            return scoreType + " score must be between 0.0 and 1.0";
        }
        return null;
    }

    /**
     * Get validation error message for email
     */
    public String getEmailValidationMessage(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Email format is invalid";
        }
        return null;
    }

    /**
     * Get validation error message for username
     */
    public String getUsernameValidationMessage(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (!isValidUsername(username)) {
            return "Username must be 3-20 characters and contain only letters, numbers, underscores, and hyphens";
        }
        return null;
    }

    /**
     * Get validation error message for password
     */
    public String getPasswordValidationMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        if (!isValidPassword(password)) {
            return "Password must contain at least one digit, one lowercase letter, and one uppercase letter";
        }
        return null;
    }

    /**
     * Sanitize text input to prevent XSS
     */
    public String sanitizeTextInput(String input) {
        if (input == null) return null;

        return input.trim()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Validate and sanitize research notes
     */
    public String sanitizeResearchNotes(String notes) {
        if (notes == null) return null;

        String sanitized = sanitizeTextInput(notes);
        return sanitized.length() > 1000 ? sanitized.substring(0, 1000) : sanitized;
    }
}