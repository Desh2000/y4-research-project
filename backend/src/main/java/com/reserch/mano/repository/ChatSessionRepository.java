package com.reserch.mano.repository;

import com.reserch.mano.model.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // User sessions
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Active sessions
    List<ChatSession> findByUserIdAndIsActiveTrue(Long userId);
    Optional<ChatSession> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(Long userId);

    // Session type filtering
    List<ChatSession> findByUserIdAndSessionType(Long userId, ChatSession.SessionType sessionType);
    List<ChatSession> findBySessionType(ChatSession.SessionType sessionType);

    // Recent sessions
    List<ChatSession> findByUserIdAndUpdatedAtAfter(Long userId, LocalDateTime date);

    // Session management
    @Modifying
    @Query("UPDATE ChatSession s SET s.isActive = false WHERE s.user.id = :userId AND s.id != :currentSessionId")
    int deactivateOtherUserSessions(@Param("userId") Long userId, @Param("currentSessionId") Long currentSessionId);

    @Modifying
    @Query("UPDATE ChatSession s SET s.sessionSummary = :summary WHERE s.id = :sessionId")
    int updateSessionSummary(@Param("sessionId") Long sessionId, @Param("summary") String summary);

    // Statistics
    long countByUserId(Long userId);
    long countByUserIdAndSessionType(Long userId, ChatSession.SessionType sessionType);

    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.sessionType = :sessionType AND s.createdAt >= :startDate")
    long countBySessionTypeAndDateAfter(@Param("sessionType") ChatSession.SessionType sessionType,
                                        @Param("startDate") LocalDateTime startDate);

    // Crisis session tracking
    @Query("SELECT s FROM ChatSession s WHERE s.sessionType = 'CRISIS' ORDER BY s.createdAt DESC")
    List<ChatSession> findCrisisSessions(Pageable pageable);
}
