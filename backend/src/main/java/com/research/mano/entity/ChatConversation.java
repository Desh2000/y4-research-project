package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat Conversation Entity for Component 3
 * Represents a chat session between user and the empathetic chatbot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_conversations", indexes = {
        @Index(name = "idx_conversation_user", columnList = "user_id"),
        @Index(name = "idx_conversation_status", columnList = "status"),
        @Index(name = "idx_conversation_crisis", columnList = "has_crisis_event")
})
public class ChatConversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @Column(name = "title")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConversationStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    // ==================== MESSAGE RELATIONSHIP ====================

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    // ==================== CONVERSATION METRICS ====================

    @Column(name = "total_messages")
    private Integer totalMessages = 0;

    @Column(name = "user_messages")
    private Integer userMessages = 0;

    @Column(name = "bot_messages")
    private Integer botMessages = 0;

    @Column(name = "average_response_time_ms")
    private Long averageResponseTimeMs;

    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes;

    // ==================== SENTIMENT TRACKING ====================

    @Column(name = "initial_sentiment_score")
    private Double initialSentimentScore;

    @Column(name = "final_sentiment_score")
    private Double finalSentimentScore;

    @Column(name = "average_sentiment_score")
    private Double averageSentimentScore;

    @Column(name = "sentiment_trend")
    @Enumerated(EnumType.STRING)
    private SentimentTrend sentimentTrend;

    // ==================== MENTAL HEALTH CONTEXT ====================

    @Column(name = "pre_conversation_stress")
    private Double preConversationStress;

    @Column(name = "pre_conversation_anxiety")
    private Double preConversationAnxiety;

    @Column(name = "pre_conversation_depression")
    private Double preConversationDepression;

    @Column(name = "post_conversation_stress")
    private Double postConversationStress;

    @Column(name = "post_conversation_anxiety")
    private Double postConversationAnxiety;

    @Column(name = "post_conversation_depression")
    private Double postConversationDepression;

    @Column(name = "mood_improvement_score")
    private Double moodImprovementScore;

    // ==================== CRISIS TRACKING ====================

    @Column(name = "has_crisis_event")
    private Boolean hasCrisisEvent = false;

    @Column(name = "highest_crisis_level")
    @Enumerated(EnumType.STRING)
    private ChatMessage.CrisisLevel highestCrisisLevel;

    @Column(name = "crisis_events_count")
    private Integer crisisEventsCount = 0;

    @Column(name = "crisis_escalated")
    private Boolean crisisEscalated = false;

    @Column(name = "escalated_to")
    private String escalatedTo; // Professional who took over

    @Column(name = "escalation_time")
    private LocalDateTime escalationTime;

    // ==================== TOPICS AND CONTEXT ====================

    @Column(name = "main_topics", columnDefinition = "TEXT")
    private String mainTopics; // JSON array of main discussion topics

    @Column(name = "detected_issues", columnDefinition = "TEXT")
    private String detectedIssues; // JSON array of mental health issues discussed

    @Column(name = "interventions_used", columnDefinition = "TEXT")
    private String interventionsUsed; // JSON array of intervention techniques

    @Column(name = "resources_provided", columnDefinition = "TEXT")
    private String resourcesProvided; // JSON array of resources shared

    // ==================== USER FEEDBACK ====================

    @Column(name = "user_rating")
    private Integer userRating; // 1-5 overall rating

    @Column(name = "was_helpful")
    private Boolean wasHelpful;

    @Column(name = "user_feedback", columnDefinition = "TEXT")
    private String userFeedback;

    @Column(name = "would_use_again")
    private Boolean wouldUseAgain;

    // ==================== TECHNICAL METADATA ====================

    @Column(name = "client_platform")
    private String clientPlatform; // web, android, ios

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "conversation_summary", columnDefinition = "TEXT")
    private String conversationSummary;

    @Column(name = "follow_up_scheduled")
    private Boolean followUpScheduled = false;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    // ==================== ENUMS ====================

    public enum ConversationStatus {
        ACTIVE,
        PAUSED,
        ENDED,
        CRISIS_ACTIVE,
        ESCALATED,
        ARCHIVED
    }

    public enum SentimentTrend {
        IMPROVING,
        STABLE,
        DECLINING,
        FLUCTUATING
    }

    // ==================== CONSTRUCTORS ====================

    public ChatConversation(User user) {
        this.user = user;
        this.sessionId = generateSessionId();
        this.status = ConversationStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    // ==================== UTILITY METHODS ====================

    private String generateSessionId() {
        return "CHAT_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setConversation(this);
        totalMessages++;
        lastMessageAt = LocalDateTime.now();

        if (message.isFromUser()) {
            userMessages++;
        } else if (message.isFromBot()) {
            botMessages++;
        }

        // Update sentiment tracking
        if (message.getSentimentScore() != null) {
            if (initialSentimentScore == null) {
                initialSentimentScore = message.getSentimentScore();
            }
            finalSentimentScore = message.getSentimentScore();
        }

        // Check for crisis
        if (message.requiresImmediateAttention()) {
            hasCrisisEvent = true;
            crisisEventsCount++;
            if (highestCrisisLevel == null ||
                    message.getCrisisLevel().ordinal() > highestCrisisLevel.ordinal()) {
                highestCrisisLevel = message.getCrisisLevel();
            }
        }
    }

    public void endConversation() {
        this.status = ConversationStatus.ENDED;
        this.endedAt = LocalDateTime.now();
        calculateSessionMetrics();
    }

    public void escalateToProfessional(String professionalId) {
        this.status = ConversationStatus.ESCALATED;
        this.crisisEscalated = true;
        this.escalatedTo = professionalId;
        this.escalationTime = LocalDateTime.now();
    }

    public void calculateSessionMetrics() {
        if (startedAt != null && endedAt != null) {
            sessionDurationMinutes = (int) java.time.Duration.between(startedAt, endedAt).toMinutes();
        }

        // Calculate average sentiment
        if (!messages.isEmpty()) {
            double totalSentiment = messages.stream()
                    .filter(m -> m.getSentimentScore() != null)
                    .mapToDouble(ChatMessage::getSentimentScore)
                    .average()
                    .orElse(0.0);
            averageSentimentScore = totalSentiment;
        }

        // Determine sentiment trend
        if (initialSentimentScore != null && finalSentimentScore != null) {
            double diff = finalSentimentScore - initialSentimentScore;
            if (diff > 0.2) sentimentTrend = SentimentTrend.IMPROVING;
            else if (diff < -0.2) sentimentTrend = SentimentTrend.DECLINING;
            else sentimentTrend = SentimentTrend.STABLE;
        }

        // Calculate mood improvement
        if (preConversationStress != null && postConversationStress != null) {
            double stressImprovement = preConversationStress - postConversationStress;
            double anxietyImprovement = (preConversationAnxiety != null && postConversationAnxiety != null)
                    ? preConversationAnxiety - postConversationAnxiety : 0;
            double depressionImprovement = (preConversationDepression != null && postConversationDepression != null)
                    ? preConversationDepression - postConversationDepression : 0;
            moodImprovementScore = (stressImprovement + anxietyImprovement + depressionImprovement) / 3.0;
        }
    }

    public boolean isActive() {
        return status == ConversationStatus.ACTIVE || status == ConversationStatus.CRISIS_ACTIVE;
    }

    public boolean needsEscalation() {
        return hasCrisisEvent &&
                (highestCrisisLevel == ChatMessage.CrisisLevel.HIGH ||
                        highestCrisisLevel == ChatMessage.CrisisLevel.CRITICAL);
    }

    public ChatMessage getLastMessage() {
        if (messages.isEmpty()) return null;
        return messages.get(messages.size() - 1);
    }

    public List<ChatMessage> getLastNMessages(int n) {
        int size = messages.size();
        if (size <= n) return messages;
        return messages.subList(size - n, size);
    }
}