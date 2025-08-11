package com.reserch.mano.repository;

import com.reserch.mano.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // Token-based lookups
    Optional<UserSession> findBySessionToken(String sessionToken);
    Optional<UserSession> findByRefreshToken(String refreshToken);

    // User sessions
    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);
    List<UserSession> findByUserId(Long userId);

    // Active sessions
    List<UserSession> findByIsActiveTrueAndExpiresAtAfter(LocalDateTime now);

    // Expired sessions
    List<UserSession> findByExpiresAtBeforeOrIsActiveFalse(LocalDateTime now);

    // Session cleanup
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt < :now")
    int deactivateExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :cutoffDate AND s.isActive = false")
    int deleteExpiredSessions(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user.id = :userId")
    int deactivateAllUserSessions(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.lastAccessed = :lastAccessed WHERE s.id = :sessionId")
    int updateLastAccessed(@Param("sessionId") Long sessionId, @Param("lastAccessed") LocalDateTime lastAccessed);

    // Statistics
    long countByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate")
    long countSessionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}