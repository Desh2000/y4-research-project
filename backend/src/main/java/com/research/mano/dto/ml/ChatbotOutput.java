package com.research.mano.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Output DTO from Chatbot NLP Service (Component 3)
 * Contains empathetic response and crisis detection results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotOutput {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("conversation_id")
    private String conversationId;

    // ==================== RESPONSE ====================

    @JsonProperty("response")
    private String response;

    @JsonProperty("response_type")
    private String responseType; // EMPATHETIC, SUPPORTIVE, CRISIS_RESPONSE, INFORMATIVE

    // ==================== SENTIMENT ANALYSIS ====================

    @JsonProperty("user_sentiment")
    private SentimentResult userSentiment;

    // ==================== CRISIS DETECTION ====================

    @JsonProperty("crisis_detected")
    private Boolean crisisDetected;

    @JsonProperty("crisis_level")
    private String crisisLevel; // NONE, LOW, MEDIUM, HIGH, CRITICAL

    @JsonProperty("crisis_type")
    private String crisisType; // SELF_HARM, SUICIDE_IDEATION, PANIC, SEVERE_DISTRESS

    @JsonProperty("crisis_keywords_found")
    private List<String> crisisKeywordsFound;

    @JsonProperty("recommended_action")
    private String recommendedAction;

    // ==================== INTENT CLASSIFICATION ====================

    @JsonProperty("detected_intent")
    private String detectedIntent;

    @JsonProperty("intent_confidence")
    private Double intentConfidence;

    @JsonProperty("detected_emotions")
    private List<EmotionScore> detectedEmotions;

    // ==================== SUGGESTED FOLLOW-UPS ====================

    @JsonProperty("suggested_responses")
    private List<String> suggestedResponses;

    @JsonProperty("suggested_activities")
    private List<String> suggestedActivities;

    // ==================== FEEDBACK LOOP (to Component 1 GAN) ====================

    @JsonProperty("feedback_data")
    private FeedbackData feedbackData;

    // ==================== METADATA ====================

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    // ==================== NESTED CLASSES ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentResult {

        @JsonProperty("sentiment")
        private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE

        @JsonProperty("sentiment_score")
        private Double sentimentScore; // -1.0 to 1.0

        @JsonProperty("confidence")
        private Double confidence;

        @JsonProperty("valence")
        private Double valence;

        @JsonProperty("arousal")
        private Double arousal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionScore {

        @JsonProperty("emotion")
        private String emotion;

        @JsonProperty("score")
        private Double score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackData {

        @JsonProperty("interaction_quality")
        private Double interactionQuality;

        @JsonProperty("user_engagement")
        private Double userEngagement;

        @JsonProperty("response_helpfulness")
        private Double responseHelpfulness;

        @JsonProperty("emotional_shift")
        private String emotionalShift; // IMPROVED, STABLE, WORSENED

        @JsonProperty("should_update_model")
        private Boolean shouldUpdateModel;
    }
}