package com.research.mano.service;

import com.research.mano.entity.ChatConversation;
import com.research.mano.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Chat Conversation Service Interface
 * Business logic for Component 3 (Empathetic Chatbot System)
 */
public interface ChatConversationService extends BaseService<ChatConversation, Long> {

    /**
     * Create a new chat message
     */
    ChatConversation createMessage(User user, String sessionId, String messageText,
                                   ChatConversation.MessageType messageType);

    /**
     * Create a bot response
     */
    ChatConversation createBotResponse(User user, String sessionId, String responseText,
                                       Double sentimentScore, String emotionDetected,
                                       Long responseTimeMs, String modelVersion);

    /**
     * Get conversation history by user
     */
    List<ChatConversation> getConversationHistory(User user);

    /**
     * Get conversation history by session
     */
    List<ChatConversation> getConversationBySession(String sessionId);

    /**
     * Get conversation history by user and session
     */
    List<ChatConversation> getConversationByUserAndSession(User user, String sessionId);

    /**
     * Find conversations with crisis keywords
     */
    List<ChatConversation> findCrisisConversations();

    /**
     * Find conversations that triggered interventions
     */
    List<ChatConversation> findInterventionTriggeredConversations();

    /**
     * Find conversations by sentiment range
     */
    List<ChatConversation> findBySentimentRange(Double minScore, Double maxScore);

    /**
     * Find negative sentiment conversations (potential crisis)
     */
    List<ChatConversation> findNegativeSentimentConversations();

    /**
     * Get recent conversations for user
     */
    List<ChatConversation> getRecentConversations(User user, int hoursBack);

    /**
     * Get conversation statistics by user
     */
    List<Object[]> getConversationStatsByUser(int daysBack);

    /**
     * Get daily conversation counts
     */
    List<Object[]> getDailyConversationCounts(LocalDateTime startDate);

    /**
     * Find conversations by emotion
     */
    List<ChatConversation> findByEmotionDetected(String emotion);

    /**
     * Get average response time by model version
     */
    List<Object[]> getAverageResponseTimeByModel();

    /**
     * Find slow response conversations
     */
    List<ChatConversation> findSlowResponseConversations(Long thresholdMs);

    /**
     * Count conversations by model version
     */
    List<Object[]> countConversationsByModelVersion();

    /**
     * Find conversations for ML training
     */
    List<ChatConversation> findConversationsForMLTraining(int daysBack);

    /**
     * Get sentiment trends over time
     */
    List<Object[]> getSentimentTrendsByDate(LocalDateTime startDate);

    /**
     * Find high-interaction users
     */
    List<Object[]> findHighInteractionUsers(int daysBack, Long interactionThreshold);

    /**
     * Process message for crisis detection
     */
    ChatConversation processCrisisDetection(Long conversationId);

    /**
     * Update sentiment analysis
     */
    ChatConversation updateSentimentAnalysis(Long conversationId, Double sentimentScore, String emotions);

    /**
     * Generate chat session ID
     */
    String generateSessionId(User user);

    /**
     * End chat session
     */
    void endChatSession(String sessionId);

    /**
     * Get active chat sessions for user
     */
    List<String> getActiveSessionsForUser(User user);

    /**
     * Analyze conversation patterns
     */
    void analyzeConversationPatterns(User user);

    /**
     * Update chatbot interaction count in the user profile
     */
    void updateChatbotInteractionCount(User user);

    /**
     * Detect and handle crisis situations
     */
    boolean detectCrisisSituation(ChatConversation conversation);

    /**
     * Trigger intervention for crisis situations
     */
    void triggerCrisisIntervention(ChatConversation conversation);

    /**
     * Get conversation context for an AI model
     */
    String getConversationContext(User user, String sessionId, int messageLimit);

    /**
     * Find conversations needing follow-up
     */
    List<ChatConversation> findConversationsNeedingFollowUp();

    /**
     * Mark conversation as requiring intervention
     */
    ChatConversation markForIntervention(Long conversationId, String reason);
}
