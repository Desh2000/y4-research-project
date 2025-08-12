package com.reserch.mano.service;
import com.reserch.mano.controller.dto.response.*;
import com.reserch.mano.controller.dto.request.*;
import com.reserch.mano.securuty.UserPrincipal;

public interface AuthService {

    AuthResponse login(AuthRequest authRequest);

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(UserPrincipal userPrincipal);

    void logoutAllSessions(UserPrincipal userPrincipal);

    void changePassword(UserPrincipal userPrincipal, ChangePasswordRequest changePasswordRequest);

    void initiatePasswordReset(String email);

    void resetPassword(String resetToken, String newPassword);

    void verifyEmail(String verificationToken);

    void resendVerificationEmail(String email);
}
