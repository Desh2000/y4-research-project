package com.research.mano.service;

import com.research.mano.dto.chat.ChatConversationDTO;
import com.research.mano.dto.chat.ChatMessageDTO;
import com.research.mano.dto.chat.ChatRequest;
import com.research.mano.dto.chat.ChatResponse;
import com.research.mano.entity.ChatMessage;
import com.research.mano.entity.ChatMessage.CrisisLevel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Chat Service Interface for Component 3
 * Handles chat conversations, message processing, and crisis detection
 */
public interface ChatService {

    // ==================== CONVERSATION MANAGEMENT ====================

    /**
     * Start a new conversation
     */
    ChatConversationDTO startConversation(Long userId, String platform);

    /**
     * Start conversation with initial mental health context
     */
    ChatConversationDTO startConversationWithContext(Long userId, String platform,
                                                     Double stressLevel, Double anxietyLevel,
                                                     Double depressionLevel);

    /**
     * Get conversation by ID
     */
    Optional<ChatConversationDTO> getConversation(Long conversationId);

    /**
     * Get conversation with messages
     */
    Optional<ChatConversationDTO> getConversationWithMessages(Long conversationId);

    /**
     * Get user's active conversation
     */
    Optional<ChatConversationDTO> getActiveConversation(Long userId);

    /**
     * Get user's conversation history
     */
    List<Object> getUserConversationHistory(Long userId);

    /**
     * Get user's conversation history paginated
     */
    List<Object> getUserConversationHistory(Long userId, int page, int size);

    /**
     * End conversation
     */
    ChatConversationDTO endConversation(Long conversationId);

    /**
     * End conversation with post-assessment
     */
    ChatConversationDTO endConversationWithAssessment(Long conversationId,
                                                      Double postStress, Double postAnxiety,
                                                      Double postDepression);

    /**
     * Pause conversation
     */
    ChatConversationDTO pauseConversation(Long conversationId);

    /**
     * Resume conversation
     */
    ChatConversationDTO resumeConversation(Long conversationId);

    // ==================== MESSAGE PROCESSING ====================

    /**
     * Process user message and generate response
     */
    ChatResponse processMessage(Long userId, ChatRequest request);

    /**
     * Process message for existing conversation
     */
    ChatResponse processMessageInConversation(Long conversationId, String message, Map<String, Object> context);

    /**
     * Get conversation messages
     */
    List<ChatMessageDTO> getConversationMessages(Long conversationId);

    /**
     * Get conversation messages paginated
     */
    List<ChatMessageDTO> getConversationMessages(Long conversationId, int page, int size);

    /**
     * Get last N messages from conversation
     */
    List<ChatMessageDTO> getLastMessages(Long conversationId, int count);

    // ==================== CRISIS DETECTION & HANDLING ====================

    /**
     * Analyze message for crisis indicators
     */
    CrisisAnalysisResult analyzeCrisisIndicators(String message);

    /**
     * Handle crisis situation
     */
    ChatResponse handleCrisis(Long conversationId, Long messageId, CrisisLevel level, List<String> indicators);

    /**
     * Escalate conversation to professional
     */
    ChatConversationDTO escalateToProfessional(Long conversationId, String reason);

    /**
     * Get unhandled crisis messages
     */
    List<ChatMessageDTO> getUnhandledCrisisMessages();

    /**
     * Get active crisis conversations
     */
    List<ChatConversationDTO> getActiveCrisisConversations();

    /**
     * Mark crisis as handled
     */
    void markCrisisHandled(Long messageId, String handledBy);

    // ==================== SENTIMENT ANALYSIS ====================

    /**
     * Analyze sentiment of a message
     */
    SentimentAnalysisResult analyzeSentiment(String message);

    /**
     * Get conversation sentiment progression
     */
    List<Map<String, Object>> getConversationSentimentProgression(Long conversationId);

    /**
     * Get user's overall sentiment trends
     */
    Map<String, Object> getUserSentimentTrends(Long userId, int days);

    // ==================== FEEDBACK ====================

    /**
     * Add feedback to a message
     */
    ChatMessageDTO addMessageFeedback(Long messageId, Integer rating, Boolean wasHelpful, String feedbackText);

    /**
     * Add feedback to conversation
     */
    ChatConversationDTO addConversationFeedback(Long conversationId, Integer rating,
                                                Boolean wasHelpful, String feedback, Boolean wouldUseAgain);

    // ==================== ANALYTICS ====================

    /**
     * Get conversation statistics for user
     */
    Map<String, Object> getUserChatStatistics(Long userId);

    /**
     * Get overall chat system statistics
     */
    Map<String, Object> getSystemChatStatistics();

    /**
     * Get crisis statistics
     */
    Map<String, Object> getCrisisStatistics(LocalDateTime since);

    // ==================== NESTED CLASSES ====================

    /**
     * Result of crisis analysis
     */
    record CrisisAnalysisResult(
            boolean crisisDetected,
            CrisisLevel level,
            List<String> indicators,
            double confidence,
            List<String> recommendedActions
    ) {}

    /**
     * Result of sentiment analysis
     */
    record SentimentAnalysisResult(
            double score,
            ChatMessage.SentimentLabel label,
            List<String> emotions,
            double confidence,
            String dominantEmotion
    ) {}
}