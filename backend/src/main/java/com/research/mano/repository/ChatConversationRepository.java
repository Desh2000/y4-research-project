package com.research.mano.repository;


import com.research.mano.entity.ChatConversation;
import com.research.mano.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Chat Conversation Repository Interface
 * Handles CRUD operations for Component 3 (Empathetic Chatbot) conversations
 */
@Repository
public interface ChatConversationRepository extends BaseRepository<ChatConversation, Long> {

    /**
     * Find conversations by user
     */
    List<ChatConversation> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find conversations by session ID
     */
    List<ChatConversation> findBySessionIdOrderByCreatedAt(String sessionId);

    /**
     * Find conversations by user and session
     */
    List<ChatConversation> findByUserAndSessionIdOrderByCreatedAt(User user, String sessionId);

    /**
     * Find conversations with crisis keywords detected
     */
    List<ChatConversation> findByCrisisKeywordsDetectedTrue();

    /**
     * Find conversations that triggered interventions
     */
    List<ChatConversation> findByInterventionTriggeredTrue();

    /**
     * Find conversations by message type
     */
    List<ChatConversation> findByMessageType(ChatConversation.MessageType messageType);

    /**
     * Find user messages (not bot responses)
     */
    List<ChatConversation> findByMessageTypeOrderByCreatedAtDesc(ChatConversation.MessageType messageType);

    /**
     * Find conversations by sentiment score range
     */
    @Query("SELECT cc FROM ChatConversation cc WHERE cc.sentimentScore >= :minScore AND cc.sentimentScore <= :maxScore")
    List<ChatConversation> findBySentimentScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Find conversations with negative sentiment (crisis detection)
     */
    @Query("SELECT cc FROM ChatConversation cc WHERE cc.sentimentScore < -0.5")
    List<ChatConversation> findNegativeSentimentConversations();

    /**
     * Find recent conversations for a user
     */
    @Query("SELECT cc FROM ChatConversation cc WHERE cc.user = :user AND cc.createdAt >= :since ORDER BY cc.createdAt DESC")
    List<ChatConversation> findRecentConversationsByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Get conversation statistics by user
     */
    @Query("SELECT cc.user.id, COUNT(cc), AVG(cc.sentimentScore), SUM(CASE WHEN cc.crisisKeywordsDetected = true THEN 1 ELSE 0 END) " +
            "FROM ChatConversation cc WHERE cc.createdAt >= :since GROUP BY cc.user.id")
    List<Object[]> getConversationStatsByUser(@Param("since") LocalDateTime since);

    /**
     * Get daily conversation counts
     */
    @Query("SELECT DATE(cc.createdAt), COUNT(cc) FROM ChatConversation cc " +
            "WHERE cc.createdAt >= :startDate GROUP BY DATE(cc.createdAt)")
    List<Object[]> getDailyConversationCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Find conversations by emotion detected
     */
    @Query("SELECT cc FROM ChatConversation cc WHERE cc.emotionDetected LIKE %:emotion%")
    List<ChatConversation> findByEmotionDetected(@Param("emotion") String emotion);

    /**
     * Get average response time by model version
     */
    @Query("SELECT cc.modelVersion, AVG(cc.responseTimeMs) FROM ChatConversation cc " +
            "WHERE cc.responseTimeMs IS NOT NULL GROUP BY cc.modelVersion")
    List<Object[]> getAverageResponseTimeByModel();

    /**
     * Find slow response conversations (for performance monitoring)
     */
    @Query("SELECT cc FROM ChatConversation cc WHERE cc.responseTimeMs > :threshold")
    List<ChatConversation> findSlowResponseConversations(@Param("threshold") Long threshold);

    /**
     * Count conversations by model version
     */
    @Query("SELECT cc.modelVersion, COUNT(cc) FROM ChatConversation cc GROUP BY cc.modelVersion")
    List<Object[]> countConversationsByModelVersion();

    /**
     * Find conversations for ML training (anonymized)
     */
    @Query("SELECT cc FROM ChatConversation cc WHERE cc.user.dataSharingConsent = true AND cc.createdAt >= :cutoffDate")
    List<ChatConversation> findConversationsForMLTraining(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get sentiment trends over time
     */
    @Query("SELECT DATE(cc.createdAt), AVG(cc.sentimentScore) FROM ChatConversation cc " +
            "WHERE cc.sentimentScore IS NOT NULL AND cc.createdAt >= :startDate " +
            "GROUP BY DATE(cc.createdAt) ORDER BY DATE(cc.createdAt)")
    List<Object[]> getSentimentTrendsByDate(@Param("startDate") LocalDateTime startDate);


    /**
     * Find high-interaction users (for engagement analysis)
     */
    @Query("SELECT cc.user, COUNT(cc) FROM ChatConversation cc " +
            "WHERE cc.createdAt >= :since GROUP BY cc.user HAVING COUNT(cc) > :threshold")
    List<Object[]> findHighInteractionUsers(@Param("since") LocalDateTime since, @Param("threshold") Long threshold);

    List<ChatConversation> findByStatus(ChatConversation.ConversationStatus conversationStatus);

    @Query("SELECT c FROM ChatConversation c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<ChatConversation> findActiveByUserId(@Param("userId") Long userId);

    List<ChatConversation> findByUserOrderByStartedAtDesc(User user);

    List<ChatConversation> findByUserAndStartedAtAfter(User user, LocalDateTime since);

    long countByStatus(ChatConversation.ConversationStatus status);
}