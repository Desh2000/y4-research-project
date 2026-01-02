package com.research.mano.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Input DTO for Chatbot NLP Service (Component 3)
 * Sends user message for empathetic response generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotInput {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("conversation_id")
    private String conversationId;

    // ==================== MESSAGE ====================

    @JsonProperty("message")
    private String message;

    @JsonProperty("message_type")
    private String messageType; // TEXT, VOICE_TRANSCRIPT

    // ==================== CONTEXT FROM OTHER COMPONENTS ====================

    @JsonProperty("user_context")
    private UserContext userContext;

    // ==================== CONVERSATION HISTORY ====================

    @JsonProperty("conversation_history")
    private List<ChatMessage> conversationHistory;

    @JsonProperty("max_history_messages")
    private Integer maxHistoryMessages = 10;

    // ==================== OPTIONS ====================

    @JsonProperty("detect_crisis")
    private Boolean detectCrisis = true;

    @JsonProperty("include_sentiment")
    private Boolean includeSentiment = true;

    @JsonProperty("response_style")
    private String responseStyle = "empathetic"; // empathetic, supportive, informative

    // ==================== NESTED CLASSES ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContext {

        @JsonProperty("current_stress_score")
        private Double currentStressScore;

        @JsonProperty("current_depression_score")
        private Double currentDepressionScore;

        @JsonProperty("current_anxiety_score")
        private Double currentAnxietyScore;

        @JsonProperty("risk_level")
        private String riskLevel;

        @JsonProperty("cluster_category")
        private String clusterCategory;

        @JsonProperty("active_interventions")
        private List<String> activeInterventions;

        @JsonProperty("recent_mood_trend")
        private String recentMoodTrend; // IMPROVING, STABLE, DECLINING
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {

        @JsonProperty("role")
        private String role; // user, assistant

        @JsonProperty("content")
        private String content;

        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("sentiment")
        private String sentiment;
    }
}