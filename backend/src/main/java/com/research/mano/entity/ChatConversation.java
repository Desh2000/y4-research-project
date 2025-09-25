package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Chat Conversation Entity for Component 3 (Empathetic Chatbot)
 * Stores chat interactions between users and the AI chatbot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chat_conversations")
public class ChatConversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "sentiment_score")
    private Double sentimentScore; // -1.0 to 1.0 (negative to positive)

    @Column(name = "emotion_detected")
    private String emotionDetected; // JSON array of detected emotions

    @Column(name = "crisis_keywords_detected")
    private Boolean crisisKeywordsDetected = false;

    @Column(name = "intervention_triggered")
    private Boolean interventionTriggered = false;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "context_data", columnDefinition = "TEXT")
    private String contextData; // JSON of conversation context

    // Constructors
    public ChatConversation() {
        super();
    }

    public ChatConversation(User user, String sessionId, String messageText, MessageType messageType) {
        this();
        this.user = user;
        this.sessionId = sessionId;
        this.messageText = messageText;
        this.messageType = messageType;
    }

    // Getters and Setters


    @Override
    public String toString() {
        return "ChatConversation{" +
                "id=" + getId() +
                ", userId=" + (user != null ? user.getId() : null) +
                ", sessionId='" + sessionId + '\'' +
                ", messageType=" + messageType +
                ", sentimentScore=" + sentimentScore +
                '}';
    }

    /**
     * Message Type Enum
     */
    public enum MessageType {
        USER_MESSAGE("Message from user"),
        BOT_RESPONSE("Response from chatbot"),
        SYSTEM_MESSAGE("System generated message"),
        INTERVENTION_ALERT("Crisis intervention alert");

        private final String description;

        MessageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
