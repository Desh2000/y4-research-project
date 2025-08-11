package com.reserch.mano.repository;


import com.reserch.mano.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Session messages
    List<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(Long chatSessionId);
    Page<ChatMessage> findByChatSessionIdOrderByCreatedAtDesc(Long chatSessionId, Pageable pageable);

    // Recent messages
    List<ChatMessage> findByChatSessionIdAndCreatedAtAfterOrderByCreatedAtAsc(
            Long chatSessionId, LocalDateTime after);

    // Message type filtering
    List<ChatMessage> findByChatSessionIdAndMessageType(
            Long chatSessionId, ChatMessage.MessageType messageType);

    // User messages across all sessions
    @Query("SELECT m FROM ChatMessage m JOIN m.chatSession s WHERE s.user.id = :userId ORDER BY m.createdAt DESC")
    Page<ChatMessage> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Sentiment analysis queries
    @Query("SELECT m FROM ChatMessage m WHERE m.sentimentScore IS NOT NULL AND m.sentimentScore < :threshold")
    List<ChatMessage> findByNegativeSentiment(@Param("threshold") Double threshold);

    @Query("SELECT AVG(m.sentimentScore) FROM ChatMessage m WHERE m.sentimentScore IS NOT NULL AND m.chatSession.user.id = :userId")
    Double getAverageSentimentByUser(@Param("userId") Long userId);

    // Crisis intervention tracking
    List<ChatMessage> findByInterventionTriggeredTrue();

    @Query("SELECT m FROM ChatMessage m WHERE m.interventionTriggered = true AND m.createdAt >= :startDate")
    List<ChatMessage> findTriggeredInterventions(@Param("startDate") LocalDateTime startDate);

    // Statistics for ML models
    long countByChatSessionId(Long chatSessionId);

    @Query("SELECT COUNT(m) FROM ChatMessage m JOIN m.chatSession s WHERE s.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.messageType = :messageType AND m.createdAt >= :startDate")
    long countByMessageTypeAndDateAfter(@Param("messageType") ChatMessage.MessageType messageType,
                                        @Param("startDate") LocalDateTime startDate);

    // Latest messages for session preview
    @Query("SELECT m FROM ChatMessage m WHERE m.chatSession.id IN :sessionIds AND " +
            "m.createdAt = (SELECT MAX(m2.createdAt) FROM ChatMessage m2 WHERE m2.chatSession.id = m.chatSession.id)")
    List<ChatMessage> findLatestMessagesBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    // Risk indicator analysis
    @Query("SELECT m FROM ChatMessage m WHERE m.riskIndicators IS NOT NULL AND m.chatSession.user.id = :userId")
    List<ChatMessage> findMessagesWithRiskIndicators(@Param("userId") Long userId);
}
