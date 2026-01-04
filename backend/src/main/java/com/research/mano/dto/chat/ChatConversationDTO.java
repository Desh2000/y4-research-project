package com.research.mano.dto.chat;

import com.research.mano.entity.ChatConversation;
import com.research.mano.entity.ChatConversation.ConversationStatus;
import com.research.mano.entity.ChatConversation.SentimentTrend;
import com.research.mano.entity.ChatMessage.CrisisLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Chat Conversations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversationDTO {

    private Long id;
    private Long userId;
    private String sessionId;
    private String title;
    private ConversationStatus status;

    // Timing
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime lastMessageAt;
    private Integer sessionDurationMinutes;

    // Metrics
    private Integer totalMessages;
    private Integer userMessages;
    private Integer botMessages;
    private Long averageResponseTimeMs;

    // Sentiment
    private Double initialSentimentScore;
    private Double finalSentimentScore;
    private Double averageSentimentScore;
    private SentimentTrend sentimentTrend;

    // Mental Health
    private Double preConversationStress;
    private Double preConversationAnxiety;
    private Double preConversationDepression;
    private Double postConversationStress;
    private Double postConversationAnxiety;
    private Double postConversationDepression;
    private Double moodImprovementScore;

    // Crisis
    private Boolean hasCrisisEvent;
    private CrisisLevel highestCrisisLevel;
    private Integer crisisEventsCount;
    private Boolean crisisEscalated;

    // Topics
    private String mainTopics;
    private String detectedIssues;

    // Feedback
    private Integer userRating;
    private Boolean wasHelpful;
    private String userFeedback;

    // Messages (optional, for detail view)
    private List<ChatMessageDTO> messages;
    private ChatMessageDTO lastMessage;

    /**
     * Convert from Entity (without messages)
     */
    public static ChatConversationDTO fromEntity(ChatConversation entity) {
        return fromEntity(entity, false);
    }

    /**
     * Convert from Entity (with optional messages)
     */
    public static ChatConversationDTO fromEntity(ChatConversation entity, boolean includeMessages) {
        if (entity == null) return null;

        ChatConversationDTOBuilder builder = ChatConversationDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .sessionId(entity.getSessionId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .endedAt(entity.getEndedAt())
                .lastMessageAt(entity.getLastMessageAt())
                .sessionDurationMinutes(entity.getSessionDurationMinutes())
                .totalMessages(entity.getTotalMessages())
                .userMessages(entity.getUserMessages())
                .botMessages(entity.getBotMessages())
                .averageResponseTimeMs(entity.getAverageResponseTimeMs())
                .initialSentimentScore(entity.getInitialSentimentScore())
                .finalSentimentScore(entity.getFinalSentimentScore())
                .averageSentimentScore(entity.getAverageSentimentScore())
                .sentimentTrend(entity.getSentimentTrend())
                .preConversationStress(entity.getPreConversationStress())
                .preConversationAnxiety(entity.getPreConversationAnxiety())
                .preConversationDepression(entity.getPreConversationDepression())
                .postConversationStress(entity.getPostConversationStress())
                .postConversationAnxiety(entity.getPostConversationAnxiety())
                .postConversationDepression(entity.getPostConversationDepression())
                .moodImprovementScore(entity.getMoodImprovementScore())
                .hasCrisisEvent(entity.getHasCrisisEvent())
                .highestCrisisLevel(entity.getHighestCrisisLevel())
                .crisisEventsCount(entity.getCrisisEventsCount())
                .crisisEscalated(entity.getCrisisEscalated())
                .mainTopics(entity.getMainTopics())
                .detectedIssues(entity.getDetectedIssues())
                .userRating(entity.getUserRating())
                .wasHelpful(entity.getWasHelpful())
                .userFeedback(entity.getUserFeedback());

        if (includeMessages && entity.getMessages() != null) {
            builder.messages(entity.getMessages().stream()
                    .map(ChatMessageDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (entity.getLastMessage() != null) {
            builder.lastMessage(ChatMessageDTO.fromEntity(entity.getLastMessage()));
        }

        return builder.build();
    }

    public void setUsername(@NotBlank @Size(max = 50) String username) {
    }

    public void setMessageText(String messageText) {
    }

    public void setMessageType(String name) {
    }

    public void setSentimentScore(Double sentimentScore) {
    }

    public void setEmotionDetected(String emotionDetected) {
    }

    public void setCrisisKeywordsDetected(Boolean crisisKeywordsDetected) {
    }

    public void setInterventionTriggered(Boolean interventionTriggered) {
    }

    public void setResponseTimeMs(Long responseTimeMs) {
    }

    public void setModelVersion(String modelVersion) {
    }

    public void setContextData(String contextData) {
    }

    public void setCreatedAt(LocalDateTime createdAt) {
    }

    public static Object fromEntity(Object o) {
        return o;
    }
}