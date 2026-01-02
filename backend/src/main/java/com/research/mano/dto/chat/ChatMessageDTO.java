package com.research.mano.dto.chat;

import com.research.mano.entity.ChatMessage;
import com.research.mano.entity.ChatMessage.SenderType;
import com.research.mano.entity.ChatMessage.SentimentLabel;
import com.research.mano.entity.ChatMessage.CrisisLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for Chat Messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    private Long id;
    private Long conversationId;
    private String content;
    private SenderType senderType;
    private LocalDateTime timestamp;

    // Sentiment
    private Double sentimentScore;
    private SentimentLabel sentimentLabel;
    private String emotionDetected;

    // Crisis
    private Boolean crisisDetected;
    private CrisisLevel crisisLevel;
    private Boolean requiresImmediateAttention;

    // Context
    private String detectedIntent;
    private String responseStrategy;

    // Feedback
    private Integer userFeedbackRating;
    private Boolean wasHelpful;

    // Metadata
    private Long responseTimeMs;
    private String modelVersion;

    /**
     * Convert from Entity
     */
    public static ChatMessageDTO fromEntity(ChatMessage entity) {
        if (entity == null) return null;

        return ChatMessageDTO.builder()
                .id(entity.getId())
                .conversationId(entity.getConversation() != null ? entity.getConversation().getId() : null)
                .content(entity.getContent())
                .senderType(entity.getSenderType())
                .timestamp(entity.getTimestamp())
                .sentimentScore(entity.getSentimentScore())
                .sentimentLabel(entity.getSentimentLabel())
                .emotionDetected(entity.getEmotionDetected())
                .crisisDetected(entity.getCrisisDetected())
                .crisisLevel(entity.getCrisisLevel())
                .requiresImmediateAttention(entity.requiresImmediateAttention())
                .detectedIntent(entity.getDetectedIntent())
                .responseStrategy(entity.getResponseStrategy())
                .userFeedbackRating(entity.getUserFeedbackRating())
                .wasHelpful(entity.getWasHelpful())
                .responseTimeMs(entity.getResponseTimeMs())
                .modelVersion(entity.getModelVersion())
                .build();
    }
}