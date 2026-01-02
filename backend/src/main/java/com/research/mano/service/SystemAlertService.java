package com.research.mano.service;


import com.research.mano.entity.SystemAlert;
import com.research.mano.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * System Alert Service Interface
 * Business logic for crisis detection, interventions, and system monitoring
 */
public interface SystemAlertService extends BaseService<SystemAlert, Long> {

    /**
     * Create a new system alert
     */
    SystemAlert createAlert(User user, SystemAlert.AlertType alertType,
                            SystemAlert.SeverityLevel severityLevel,
                            String title, String message, String triggerSource);

    /**
     * Create a crisis alert
     */
    SystemAlert createCrisisAlert(User user, String title, String message,
                                  String triggerSource, String triggerData);

    /**
     * Create a high-risk prediction alert
     */
    SystemAlert createHighRiskPredictionAlert(User user, Double riskScore, String triggerData);

    /**
     * Create a chat crisis alert
     */
    SystemAlert createChatCrisisAlert(User user, String chatMessage, String sessionId);

    /**
     * Get alerts by user
     */
    List<SystemAlert> getAlertsByUser(User user);

    /**
     * Get unresolved alerts
     */
    List<SystemAlert> getUnresolvedAlerts();

    /**
     * Get crisis alerts
     */
    List<SystemAlert> getCrisisAlerts();

    /**
     * Get critical alerts
     */
    List<SystemAlert> getCriticalAlerts();

    /**
     * Get alerts by severity level
     */
    List<SystemAlert> getAlertsBySeverity(SystemAlert.SeverityLevel severityLevel);

    /**
     * Get alerts by type
     */
    List<SystemAlert> getAlertsByType(SystemAlert.AlertType alertType);

    /**
     * Get alerts assigned to professional
     */
    List<SystemAlert> getAlertsAssignedTo(String assignedTo);

    /**
     * Get alerts by trigger source
     */
    List<SystemAlert> getAlertsByTriggerSource(String triggerSource);

    /**
     * Get unresolved alerts for user
     */
    List<SystemAlert> getUnresolvedAlertsForUser(User user);

    /**
     * Get alerts within date range
     */
    List<SystemAlert> getAlertsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find alerts needing emergency notification
     */
    List<SystemAlert> getAlertsNeedingEmergencyNotification();

    /**
     * Find alerts needing professional notification
     */
    List<SystemAlert> getAlertsNeedingProfessionalNotification();

    /**
     * Find overdue alerts
     */
    List<SystemAlert> getOverdueAlerts(int hoursOverdue);

    /**
     * Resolve alert
     */
    SystemAlert resolveAlert(Long alertId, String resolvedBy, String resolutionNotes, String actionTaken);

    /**
     * Assign alert to professional
     */
    SystemAlert assignToProfessional(Long alertId, String assignedTo);

    /**
     * Mark emergency contact as notified
     */
    SystemAlert markEmergencyContactNotified(Long alertId);

    /**
     * Mark professional as notified
     */
    SystemAlert markProfessionalNotified(Long alertId);

    /**
     * Update action taken
     */
    SystemAlert updateActionTaken(Long alertId, String actionTaken);

    /**
     * Escalate alert severity
     */
    SystemAlert escalateAlert(Long alertId, SystemAlert.SeverityLevel newSeverity, String reason);

    /**
     * Count alerts by type
     */
    List<Object[]> countAlertsByType();

    /**
     * Count alerts by severity
     */
    List<Object[]> countAlertsBySeverity();

    /**
     * Get daily alert counts
     */
    List<Object[]> getDailyAlertCounts(LocalDateTime startDate);

    /**
     * Get alert resolution statistics
     */
    Object[] getAlertResolutionStats(int daysBack);

    /**
     * Get most frequently alerted users
     */
    List<Object[]> getMostFrequentlyAlertedUsers();

    /**
     * Get alerts by component sources
     */
    List<SystemAlert> getAlertsByComponentSources(List<String> components);

    /**
     * Get recent high-priority alerts
     */
    List<SystemAlert> getRecentHighPriorityAlerts(int hoursBack);

    /**
     * Get user risk trend based on alerts
     */
    List<Object[]> getUserRiskTrend(LocalDateTime startDate);

    /**
     * Get system-wide alerts
     */
    List<SystemAlert> getSystemWideAlerts();

    /**
     * Count unresolved alerts by severity
     */
    List<Object[]> countUnresolvedAlertsBySeverity();

    /**
     * Process automatic crisis intervention
     */
    void processAutomaticCrisisIntervention(SystemAlert alert);

    /**
     * Send emergency notifications
     */
    void sendEmergencyNotifications(SystemAlert alert);

    /**
     * Send professional notifications
     */
    void sendProfessionalNotifications(SystemAlert alert);

    /**
     * Clean up resolved alerts (archive old ones)
     */
    void cleanupResolvedAlerts(int daysOld);

    /**
     * Generate alert summary report
     */
    String generateAlertSummaryReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Check for alert patterns (recurring issues)
     */
    List<SystemAlert> checkForAlertPatterns(User user, int daysBack);

    /**
     * Auto-resolve system alerts
     */
    void autoResolveSystemAlerts();
}
