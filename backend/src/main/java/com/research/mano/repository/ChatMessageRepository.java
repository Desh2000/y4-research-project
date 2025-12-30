package com.research.mano.repository;

import com.research.mano.entity.ChatMessage;
import com.research.mano.entity.ChatMessage.SenderType;
import com.research.mano.entity.ChatMessage.CrisisLevel;
import com.research.mano.entity.ChatConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Chat Message Repository for Component 3
 */
@Repository
public interface ChatMessageRepository extends BaseRepository<ChatMessage, Long> {

    // ==================== BASIC QUERIES ====================

    List<ChatMessage> findByConversation(ChatConversation conversation);

    List<ChatMessage> findByConversationOrderByTimestampAsc(ChatConversation conversation);

    List<ChatMessage> findByConversationOrderByTimestampDesc(ChatConversation conversation);

    Page<ChatMessage> findByConversationOrderByTimestampDesc(ChatConversation conversation, Pageable pageable);

    List<ChatMessage> findByConversationId(Long conversationId);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp ASC")
    List<ChatMessage> findByConversationIdOrdered(@Param("conversationId") Long conversationId);

    // ==================== SENDER TYPE QUERIES ====================

    List<ChatMessage> findByConversationAndSenderType(ChatConversation conversation, SenderType senderType);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderType = :senderType ORDER BY m.timestamp ASC")
    List<ChatMessage> findByConversationIdAndSenderType(
            @Param("conversationId") Long conversationId,
            @Param("senderType") SenderType senderType);

    // ==================== CRISIS QUERIES ====================

    List<ChatMessage> findByCrisisDetectedTrue();

    List<ChatMessage> findByCrisisLevel(CrisisLevel crisisLevel);

    @Query("SELECT m FROM ChatMessage m WHERE m.crisisDetected = true AND m.crisisLevel IN ('HIGH', 'CRITICAL') ORDER BY m.timestamp DESC")
    List<ChatMessage> findHighPriorityCrisisMessages();

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.crisisDetected = true ORDER BY m.timestamp ASC")
    List<ChatMessage> findCrisisMessagesByConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT m FROM ChatMessage m WHERE m.crisisDetected = true AND m.crisisResponseTriggered = false ORDER BY m.timestamp ASC")
    List<ChatMessage> findUnhandledCrisisMessages();

    @Query("SELECT m FROM ChatMessage m WHERE m.crisisDetected = true AND m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<ChatMessage> findRecentCrisisMessages(@Param("since") LocalDateTime since);

    // ==================== SENTIMENT QUERIES ====================

    @Query("SELECT AVG(m.sentimentScore) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.sentimentScore IS NOT NULL")
    Double getAverageSentimentByConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.sentimentScore < :threshold ORDER BY m.timestamp ASC")
    List<ChatMessage> findNegativeSentimentMessages(
            @Param("conversationId") Long conversationId,
            @Param("threshold") Double threshold);

    @Query("SELECT m.sentimentLabel, COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId GROUP BY m.sentimentLabel")
    List<Object[]> getSentimentDistributionByConversation(@Param("conversationId") Long conversationId);

    // ==================== TIME-BASED QUERIES ====================

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.timestamp BETWEEN :start AND :end ORDER BY m.timestamp ASC")
    List<ChatMessage> findByConversationAndTimeRange(
            @Param("conversationId") Long conversationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT m FROM ChatMessage m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<ChatMessage> findRecentMessages(@Param("since") LocalDateTime since);

    // ==================== LATEST MESSAGE QUERIES ====================

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp DESC LIMIT 1")
    Optional<ChatMessage> findLatestByConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatestNByConversation(@Param("conversationId") Long conversationId, Pageable pageable);

    // ==================== FEEDBACK QUERIES ====================

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.userFeedbackRating IS NOT NULL")
    List<ChatMessage> findRatedMessages(@Param("conversationId") Long conversationId);

    @Query("SELECT AVG(m.userFeedbackRating) FROM ChatMessage m WHERE m.senderType = 'BOT' AND m.userFeedbackRating IS NOT NULL")
    Double getAverageBotRating();

    @Query("SELECT m FROM ChatMessage m WHERE m.wasHelpful = false AND m.senderType = 'BOT'")
    List<ChatMessage> findUnhelpfulBotResponses();

    // ==================== INTENT AND TOPIC QUERIES ====================

    @Query("SELECT m.detectedIntent, COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.detectedIntent IS NOT NULL GROUP BY m.detectedIntent")
    List<Object[]> getIntentDistributionByConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT m FROM ChatMessage m WHERE m.detectedIntent = :intent ORDER BY m.timestamp DESC")
    List<ChatMessage> findByIntent(@Param("intent") String intent);

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId")
    Long countByConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderType = :senderType")
    Long countByConversationAndSenderType(
            @Param("conversationId") Long conversationId,
            @Param("senderType") SenderType senderType);

    @Query("SELECT AVG(m.responseTimeMs) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.senderType = 'BOT' AND m.responseTimeMs IS NOT NULL")
    Double getAverageResponseTimeByConversation(@Param("conversationId") Long conversationId);

    // ==================== UPDATE QUERIES ====================

    @Modifying
    @Query("UPDATE ChatMessage m SET m.crisisResponseTriggered = true WHERE m.id = :messageId")
    int markCrisisResponseTriggered(@Param("messageId") Long messageId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isDeleted = true WHERE m.id = :messageId")
    int softDeleteMessage(@Param("messageId") Long messageId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.userFeedbackRating = :rating, m.wasHelpful = :helpful, m.userFeedbackText = :feedback WHERE m.id = :messageId")
    int updateMessageFeedback(
            @Param("messageId") Long messageId,
            @Param("rating") Integer rating,
            @Param("helpful") Boolean helpful,
            @Param("feedback") String feedback);
}