package com.reserch.mano.service;

import com.reserch.mano.controller.dto.response.AuthResponse;
import com.reserch.mano.securuty.UserPrincipal;

public interface TokenService {

    AuthResponse generateTokens(UserPrincipal userPrincipal, String deviceInfo,
                                String ipAddress, String userAgent);

    AuthResponse refreshAccessToken(String refreshToken);

    void revokeToken(String token);

    void revokeAllUserTokens(Long userId);

    boolean validateAccessToken(String token);

    boolean validateRefreshToken(String token);

    UserPrincipal getUserFromToken(String token);

    Long getUserIdFromToken(String token);

    void cleanupExpiredTokens();
}