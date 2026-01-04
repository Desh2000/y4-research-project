package com.research.mano.service.Impl;

import com.research.mano.entity.SystemAlert;
import com.research.mano.entity.User;
import com.research.mano.repository.SystemAlertRepository;
import com.research.mano.service.SystemAlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * System Alert Service Implementation
 * Handles crisis detection, interventions, and system monitoring business logic
 */
@Service
@Transactional
public class SystemAlertServiceImpl implements SystemAlertService {

    private final SystemAlertRepository systemAlertRepository;

    public SystemAlertServiceImpl(SystemAlertRepository systemAlertRepository) {
        this.systemAlertRepository = systemAlertRepository;
    }

    @Override
    public SystemAlert save(SystemAlert systemAlert) {
        return systemAlertRepository.save(systemAlert);
    }

    @Override
    public List<SystemAlert> saveAll(List<SystemAlert> systemAlerts) {
        return systemAlertRepository.saveAll(systemAlerts);
    }

    @Override
    public Optional<SystemAlert> findById(Long id) {
        return systemAlertRepository.findById(id);
    }

    @Override
    public List<SystemAlert> findAll() {
        return systemAlertRepository.findAll();
    }

    @Override
    public Page<SystemAlert> findAll(Pageable pageable) {
        return systemAlertRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return systemAlertRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        systemAlertRepository.deleteById(id);
    }

    @Override
    public void delete(SystemAlert systemAlert) {
        systemAlertRepository.delete(systemAlert);
    }

    @Override
    public long count() {
        return systemAlertRepository.count();
    }

    @Override
    public SystemAlert createAlert(User user, SystemAlert.AlertType alertType,
                                   SystemAlert.SeverityLevel severityLevel,
                                   String title, String message, String triggerSource) {
        SystemAlert alert = new SystemAlert();
        alert.setUser(user);
        alert.setAlertType(alertType);
        alert.setSeverityLevel(severityLevel);
        alert.setAlertTitle(title);
        alert.setAlertMessage(message);
        alert.setTriggerSource(triggerSource);

        SystemAlert savedAlert = systemAlertRepository.save(alert);

        // Process automatic notifications based on severity
        if (severityLevel == SystemAlert.SeverityLevel.CRITICAL) {
            processAutomaticCrisisIntervention(savedAlert);
        }

        return savedAlert;
    }

    @Override
    public SystemAlert createCrisisAlert(User user, String title, String message,
                                         String triggerSource, String triggerData) {
        SystemAlert alert = createAlert(user, SystemAlert.AlertType.CRISIS_CHAT_DETECTED,
                SystemAlert.SeverityLevel.CRITICAL, title, message, triggerSource);
        alert.setTriggerData(triggerData);
        alert.setIsCrisis(true);

        return systemAlertRepository.save(alert);
    }

    @Override
    public SystemAlert createHighRiskPredictionAlert(User user, Double riskScore, String triggerData) {
        String title = "High Risk Mental Health Prediction";
        String message = String.format("User %s has received a high-risk mental health prediction with overall score: %.2f",
                user.getUsername(), riskScore);

        SystemAlert alert = createAlert(user, SystemAlert.AlertType.HIGH_RISK_PREDICTION,
                SystemAlert.SeverityLevel.HIGH, title, message, "Component-2-LSTM");
        alert.setTriggerData(triggerData);

        return alert;
    }

    @Override
    public SystemAlert createChatCrisisAlert(User user, String chatMessage, String sessionId) {
        String title = "Crisis Keywords Detected in Chat";
        String message = "Crisis-related content detected in user conversation";
        String triggerData = String.format("{\"sessionId\":\"%s\",\"messagePreview\":\"%s\"}",
                sessionId, chatMessage.length() > 100 ?
                        chatMessage.substring(0, 100) + "..." : chatMessage);

        return createCrisisAlert(user, title, message, "Component-3-Chatbot", triggerData);
    }

    @Override
    public List<SystemAlert> getAlertsByUser(User user) {
        return systemAlertRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<SystemAlert> getUnresolvedAlerts() {
        return systemAlertRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
    }

    @Override
    public List<SystemAlert> getCrisisAlerts() {
        return systemAlertRepository.findByIsCrisisTrueOrderByCreatedAtDesc();
    }

    @Override
    public List<SystemAlert> getCriticalAlerts() {
        return systemAlertRepository.findBySeverityLevelOrderByCreatedAtDesc(SystemAlert.SeverityLevel.CRITICAL);
    }

    @Override
    public List<SystemAlert> getAlertsBySeverity(SystemAlert.SeverityLevel severityLevel) {
        return systemAlertRepository.findBySeverityLevel(severityLevel);
    }

    @Override
    public List<SystemAlert> getAlertsByType(SystemAlert.AlertType alertType) {
        return systemAlertRepository.findByAlertType(alertType);
    }

    @Override
    public List<SystemAlert> getAlertsAssignedTo(String assignedTo) {
        return systemAlertRepository.findByAssignedToOrderByCreatedAtDesc(assignedTo);
    }

    @Override
    public List<SystemAlert> getAlertsByTriggerSource(String triggerSource) {
        return systemAlertRepository.findByTriggerSource(triggerSource);
    }

    @Override
    public List<SystemAlert> getUnresolvedAlertsForUser(User user) {
        return systemAlertRepository.findByUserAndIsResolvedFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public List<SystemAlert> getAlertsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return systemAlertRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<SystemAlert> getAlertsNeedingEmergencyNotification() {
        return systemAlertRepository.findAlertsNeedingEmergencyNotification();
    }

    @Override
    public List<SystemAlert> getAlertsNeedingProfessionalNotification() {
        return systemAlertRepository.findAlertsNeedingProfessionalNotification();
    }

    @Override
    public List<SystemAlert> getOverdueAlerts(int hoursOverdue) {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(hoursOverdue);
        return systemAlertRepository.findOverdueAlerts(overdueThreshold);
    }

    @Override
    public SystemAlert resolveAlert(Long alertId, String resolvedBy, String resolutionNotes, String actionTaken) {
        SystemAlert alert = systemAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.markAsResolved(resolvedBy, resolutionNotes);
        alert.setActionTaken(actionTaken);

        return systemAlertRepository.save(alert);
    }

    @Override
    public SystemAlert assignToProfessional(Long alertId, String assignedTo) {
        systemAlertRepository.assignToProfessional(alertId, assignedTo);
        return systemAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
    }

    @Override
    public SystemAlert markEmergencyContactNotified(Long alertId) {
        systemAlertRepository.markEmergencyContactNotified(alertId, LocalDateTime.now());
        return systemAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
    }

    @Override
    public SystemAlert markProfessionalNotified(Long alertId) {
        systemAlertRepository.markProfessionalNotified(alertId, LocalDateTime.now());
        return systemAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
    }

    @Override
    public SystemAlert updateActionTaken(Long alertId, String actionTaken) {
        systemAlertRepository.updateActionTaken(alertId, actionTaken);
        return systemAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
    }

    @Override
    public SystemAlert escalateAlert(Long alertId, SystemAlert.SeverityLevel newSeverity, String reason) {
        SystemAlert alert = systemAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setSeverityLevel(newSeverity);
        alert.setResolutionNotes((alert.getResolutionNotes() != null ? alert.getResolutionNotes() + "; " : "") +
                "Escalated: " + reason);

        if (newSeverity == SystemAlert.SeverityLevel.CRITICAL) {
            processAutomaticCrisisIntervention(alert);
        }

        return systemAlertRepository.save(alert);
    }

    @Override
    public List<Object[]> countAlertsByType() {
        return systemAlertRepository.countAlertsByType();
    }

    @Override
    public List<Object[]> countAlertsBySeverity() {
        return systemAlertRepository.countAlertsBySeverity();
    }

    @Override
    public List<Object[]> getDailyAlertCounts(LocalDateTime startDate) {
        return systemAlertRepository.getDailyAlertCounts(startDate);
    }

    @Override
    public Object[] getAlertResolutionStats(int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return systemAlertRepository.getAlertResolutionStats(since);
    }

    @Override
    public List<Object[]> getMostFrequentlyAlertedUsers() {
        return systemAlertRepository.getMostFrequentlyAlertedUsers();
    }

    @Override
    public List<SystemAlert> getAlertsByComponentSources(List<String> components) {
        return systemAlertRepository.findByComponentSources(components);
    }

    @Override
    public List<SystemAlert> getRecentHighPriorityAlerts(int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return systemAlertRepository.findRecentHighPriorityAlerts(since);
    }

    @Override
    public List<Object[]> getUserRiskTrend(LocalDateTime startDate) {
        return systemAlertRepository.getUserRiskTrend(startDate);
    }

    @Override
    public List<SystemAlert> getSystemWideAlerts() {
        return systemAlertRepository.findByUserIsNullOrderByCreatedAtDesc();
    }

    @Override
    public List<Object[]> countUnresolvedAlertsBySeverity() {
        return systemAlertRepository.countUnresolvedAlertsBySeverity();
    }

    @Override
    public void processAutomaticCrisisIntervention(SystemAlert alert) {
        if (alert.requiresImmediateAttention()) {
            // Send emergency notifications
            sendEmergencyNotifications(alert);
            sendProfessionalNotifications(alert);

            // Auto-assign to crisis response team
            assignToProfessional(alert.getId(), "crisis-response-team");
        }
    }

    @Override
    public void sendEmergencyNotifications(SystemAlert alert) {
        if (alert.getUser() != null && alert.getUser().getEmergencyContactPhone() != null) {
            // TODO: Implement SMS/email notification to emergency contact
            markEmergencyContactNotified(alert.getId());
        }
    }

    @Override
    public void sendProfessionalNotifications(SystemAlert alert) {
        // TODO: Implement notification to mental health professionals
        markProfessionalNotified(alert.getId());
    }

    @Override
    public void cleanupResolvedAlerts(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<SystemAlert> oldResolvedAlerts = systemAlertRepository.findByDateRange(
                        LocalDateTime.now().minusYears(10), cutoffDate
                ).stream()
                .filter(alert -> Boolean.TRUE.equals(alert.getIsResolved()))
                .toList();

        // Archive or delete old resolved alerts
        systemAlertRepository.deleteAll(oldResolvedAlerts);
    }

    @Override
    public String generateAlertSummaryReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<SystemAlert> alerts = getAlertsByDateRange(startDate, endDate);

        long totalAlerts = alerts.size();
        long resolvedAlerts = alerts.stream().filter(a -> Boolean.TRUE.equals(a.getIsResolved())).count();
        long crisisAlerts = alerts.stream().filter(a -> Boolean.TRUE.equals(a.getIsCrisis())).count();

        StringBuilder report = new StringBuilder();
        report.append("Alert Summary Report\n");
        report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
        report.append("Total Alerts: ").append(totalAlerts).append("\n");
        report.append("Resolved Alerts: ").append(resolvedAlerts).append("\n");
        report.append("Crisis Alerts: ").append(crisisAlerts).append("\n");
        report.append("Resolution Rate: ").append(totalAlerts > 0 ? (resolvedAlerts * 100.0 / totalAlerts) : 0).append("%\n");

        return report.toString();
    }

    @Override
    public List<SystemAlert> checkForAlertPatterns(User user, int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return getAlertsByUser(user).stream()
                .filter(alert -> alert.getCreatedAt().isAfter(since))
                .toList();
    }

    @Override
    public void autoResolveSystemAlerts() {
        // Auto-resolve certain types of system alerts after specific conditions
        List<SystemAlert> systemAlerts = getSystemWideAlerts().stream()
                .filter(alert -> !Boolean.TRUE.equals(alert.getIsResolved()))
                .filter(alert -> alert.getAlertType() == SystemAlert.AlertType.SYSTEM_ERROR ||
                        alert.getAlertType() == SystemAlert.AlertType.DATA_ANOMALY)
                .filter(alert -> alert.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24)))
                .toList();

        for (SystemAlert alert : systemAlerts) {
            resolveAlert(alert.getId(), "SYSTEM", "Auto-resolved after 24 hours", "No action required");
        }
    }
}
