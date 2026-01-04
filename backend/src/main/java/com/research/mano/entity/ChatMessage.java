package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Chat Message Entity for Component 3
 * Stores individual messages in chat conversations
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_message_conversation", columnList = "conversation_id"),
        @Index(name = "idx_chat_message_timestamp", columnList = "timestamp"),
        @Index(name = "idx_chat_message_crisis", columnList = "crisis_detected")
})
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // ==================== SENTIMENT ANALYSIS ====================

    @Column(name = "sentiment_score")
    private Double sentimentScore; // -1.0 (negative) to 1.0 (positive)

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_label")
    private SentimentLabel sentimentLabel;

    @Column(name = "emotion_detected")
    private String emotionDetected; // JSON array of detected emotions

    @Column(name = "emotion_confidence")
    private Double emotionConfidence;

    // ==================== CRISIS DETECTION ====================

    @Column(name = "crisis_detected")
    private Boolean crisisDetected = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "crisis_level")
    private CrisisLevel crisisLevel;

    @Column(name = "crisis_indicators", columnDefinition = "TEXT")
    private String crisisIndicators; // JSON array of detected indicators

    @Column(name = "crisis_response_triggered")
    private Boolean crisisResponseTriggered = false;

    @Column(name = "crisis_handled_by")
    private String crisisHandledBy; // Professional who handled the crisis

    @Column(name = "crisis_resolution_time")
    private LocalDateTime crisisResolutionTime;

    // ==================== INTENT AND CONTEXT ====================

    @Column(name = "detected_intent")
    private String detectedIntent; // e.g., "seeking_support", "venting", "asking_question"

    @Column(name = "intent_confidence")
    private Double intentConfidence;

    @Column(name = "topics_discussed", columnDefinition = "TEXT")
    private String topicsDiscussed; // JSON array of topics

    @Column(name = "context_tags", columnDefinition = "TEXT")
    private String contextTags; // JSON array of context tags

    // ==================== RESPONSE METADATA ====================

    @Column(name = "response_strategy")
    private String responseStrategy; // e.g., "empathetic_listening", "cbt_technique", "grounding"

    @Column(name = "intervention_applied")
    private String interventionApplied; // Intervention technique used

    @Column(name = "follow_up_suggested")
    private Boolean followUpSuggested = false;

    @Column(name = "resources_shared", columnDefinition = "TEXT")
    private String resourcesShared; // JSON array of shared resources

    // ==================== ENGAGEMENT METRICS ====================

    @Column(name = "response_time_ms")
    private Long responseTimeMs; // Time taken to generate response

    @Column(name = "user_feedback_rating")
    private Integer userFeedbackRating; // 1-5 rating

    @Column(name = "was_helpful")
    private Boolean wasHelpful;

    @Column(name = "user_feedback_text", columnDefinition = "TEXT")
    private String userFeedbackText;

    // ==================== TECHNICAL METADATA ====================

    @Column(name = "model_version")
    private String modelVersion; // AI model version used

    @Column(name = "processing_metadata", columnDefinition = "TEXT")
    private String processingMetadata; // JSON of processing details

    @Column(name = "is_edited")
    private Boolean isEdited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // ==================== CONSTRUCTORS ====================

    public ChatMessage(ChatConversation conversation, String content, SenderType senderType) {
        this.conversation = conversation;
        this.content = content;
        this.senderType = senderType;
        this.timestamp = LocalDateTime.now();
    }

    // ==================== ENUMS ====================

    public enum SenderType {
        USER,
        BOT,
        SYSTEM,
        HEALTHCARE_PROFESSIONAL
    }

    public enum SentimentLabel {
        VERY_NEGATIVE,
        NEGATIVE,
        NEUTRAL,
        POSITIVE,
        VERY_POSITIVE
    }

    public enum CrisisLevel {
        NONE,
        LOW,           // Mild distress indicators
        MEDIUM,        // Significant distress, needs monitoring
        HIGH,          // Urgent - potential self-harm thoughts
        CRITICAL       // Emergency - immediate intervention needed
    }

    // ==================== UTILITY METHODS ====================

    public boolean requiresImmediateAttention() {
        return crisisDetected != null && crisisDetected &&
                (crisisLevel == CrisisLevel.HIGH || crisisLevel == CrisisLevel.CRITICAL);
    }

    public boolean isFromUser() {
        return senderType == SenderType.USER;
    }

    public boolean isFromBot() {
        return senderType == SenderType.BOT;
    }

    public static SentimentLabel getSentimentLabel(Double score) {
        if (score == null) return null;
        if (score <= -0.6) return SentimentLabel.VERY_NEGATIVE;
        if (score <= -0.2) return SentimentLabel.NEGATIVE;
        if (score <= 0.2) return SentimentLabel.NEUTRAL;
        if (score <= 0.6) return SentimentLabel.POSITIVE;
        return SentimentLabel.VERY_POSITIVE;
    }

    public void markAsCrisis(CrisisLevel level, String indicators) {
        this.crisisDetected = true;
        this.crisisLevel = level;
        this.crisisIndicators = indicators;
    }

    public void resolveCrisis(String handledBy) {
        this.crisisHandledBy = handledBy;
        this.crisisResolutionTime = LocalDateTime.now();
    }
}