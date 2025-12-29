package com.research.mano.controller.responce;

import com.research.mano.entity.ChatMessage.CrisisLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for chat messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    /**
     * Conversation info
     */
    private Long conversationId;
    private String sessionId;
    private Boolean isNewConversation;

    /**
     * Bot response message
     */
    private Long messageId;
    private String message;
    private LocalDateTime timestamp;

    /**
     * Response metadata
     */
    private Long responseTimeMs;
    private String modelVersion;
    private String responseStrategy;

    /**
     * Sentiment analysis of user message
     */
    private Double userMessageSentiment;
    private String userMessageEmotion;
    private String userMessageIntent;

    /**
     * Crisis detection
     */
    private Boolean crisisDetected;
    private CrisisLevel crisisLevel;
    private List<String> crisisIndicators;
    private Boolean escalationTriggered;
    private String escalationMessage;

    /**
     * Suggestions and resources
     */
    private List<String> suggestedActions;
    private List<ResourceSuggestion> resourceSuggestions;
    private Boolean followUpSuggested;

    /**
     * Conversation status
     */
    private String conversationStatus;
    private Integer messageCount;

    /**
     * Error info (if any)
     */
    private Boolean hasError;
    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResourceSuggestion {
        private String title;
        private String description;
        private String url;
        private String type; // article, video, exercise, hotline
    }

    /**
     * Create error response
     */
    public static ChatResponse error(String errorMessage) {
        return ChatResponse.builder()
                .hasError(true)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create crisis response
     */
    public static ChatResponse crisisResponse(Long conversationId, CrisisLevel level,
                                              String message, List<String> indicators) {
        return ChatResponse.builder()
                .conversationId(conversationId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .crisisDetected(true)
                .crisisLevel(level)
                .crisisIndicators(indicators)
                .escalationTriggered(level == CrisisLevel.HIGH || level == CrisisLevel.CRITICAL)
                .build();
    }
}