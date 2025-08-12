package com.reserch.mano.service;

import java.util.Map;

public interface EmailService {

    void sendVerificationEmail(String email, String verificationToken);

    void sendPasswordResetEmail(String email, String resetToken);

    void sendWelcomeEmail(String email, String username);

    void sendAccountDeactivationEmail(String email, String username);

    void sendCrisisAlertEmail(String email, String username);

    void sendWeeklyReportEmail(String email, Map<String, Object> reportData);

    void sendCustomEmail(String email, String subject, String content);

    void sendTemplateEmail(String email, String templateName, Map<String, Object> variables);

    boolean isEmailServiceAvailable();

    void validateEmailAddress(String email);
}

