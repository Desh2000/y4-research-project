package com.reserch.mano.service;

import com.reserch.mano.model.UserSession;
import com.reserch.mano.securuty.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSessionService {

    UserSession createSession(UserPrincipal userPrincipal, String sessionToken,
                              String refreshToken, String deviceInfo, String ipAddress,
                              String userAgent, LocalDateTime expiresAt);

    Optional<UserSession> getSessionByToken(String sessionToken);

    Optional<UserSession> getSessionByRefreshToken(String refreshToken);

    List<UserSession> getActiveUserSessions(Long userId);

    List<UserSession> getAllUserSessions(Long userId);

    void updateLastAccessed(Long sessionId, LocalDateTime lastAccessed);

    void deactivateSession(String sessionToken);

    void deactivateAllUserSessions(Long userId);

    void deactivateExpiredSessions();

    void cleanupExpiredSessions(int daysOld);

    boolean isSessionActive(String sessionToken);

    long getActiveSessionCount(Long userId);

    void validateSessionLimit(Long userId, int maxSessions);
}