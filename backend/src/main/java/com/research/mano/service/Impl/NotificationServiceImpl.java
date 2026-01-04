package com.research.mano.service.Impl;

import com.research.mano.entity.SystemAlert;
import com.research.mano.entity.User;
import com.research.mano.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification Service Implementation
 * Handles real-time notifications via WebSocket
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ==================== USER NOTIFICATIONS ====================

    @Override
    public void sendToUser(Long userId, String destination, Object payload) {
        try {
            String userDestination = "/user/" + userId + destination;
            messagingTemplate.convertAndSend(userDestination, payload);
            logger.debug("Sent notification to user {} at {}", userId, destination);
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendToUser(String username, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(username, destination, payload);
            logger.debug("Sent notification to user {} at {}", username, destination);
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", username, e.getMessage());
        }
    }

    @Override
    public void notifyPredictionResult(Long userId, Map<String, Object> predictionData) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PREDICTION_RESULT");
        notification.put("timestamp", LocalDateTime.now());
        notification.put("data", predictionData);

        sendToUser(userId, "/queue/predictions", notification);
        logger.info("Notified user {} of prediction result", userId);
    }

    @Override
    public void notifyClusterChange(Long userId, String oldCluster, String newCluster) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CLUSTER_CHANGE");
        notification.put("timestamp", LocalDateTime.now());
        notification.put("oldCluster", oldCluster);
        notification.put("newCluster", newCluster);
        notification.put("message", "Your peer support group has been updated based on your recent progress.");

        sendToUser(userId, "/queue/cluster-updates", notification);
        logger.info("Notified user {} of cluster change: {} -> {}", userId, oldCluster, newCluster);
    }

    @Override
    public void notifyInterventionRecommendation(Long userId, Map<String, Object> interventionData) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "INTERVENTION_RECOMMENDATION");
        notification.put("timestamp", LocalDateTime.now());
        notification.put("data", interventionData);
        notification.put("message", "A new wellness activity has been recommended for you.");

        sendToUser(userId, "/queue/interventions", notification);
        logger.info("Notified user {} of intervention recommendation", userId);
    }

    @Override
    public void notifyChatResponse(Long userId, String message, Long conversationId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CHAT_RESPONSE");
        notification.put("timestamp", LocalDateTime.now());
        notification.put("conversationId", conversationId);
        notification.put("preview", message.length() > 100 ? message.substring(0, 100) + "..." : message);

        sendToUser(userId, "/queue/chat-notifications", notification);
    }

    // ==================== PROFESSIONAL NOTIFICATIONS ====================

    @Override
    public void sendToAllProfessionals(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend("/topic/professionals" + destination, payload);
            logger.debug("Sent notification to all professionals at {}", destination);
        } catch (Exception e) {
            logger.error("Failed to send notification to professionals: {}", e.getMessage());
        }
    }

    @Override
    public void sendToProfessional(String professionalId, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(professionalId, destination, payload);
            logger.debug("Sent notification to professional {} at {}", professionalId, destination);
        } catch (Exception e) {
            logger.error("Failed to send notification to professional {}: {}", professionalId, e.getMessage());
        }
    }

    @Override
    public void notifyCrisisDetected(SystemAlert alert) {
        Map<String, Object> crisisNotification = new HashMap<>();
        crisisNotification.put("type", "CRISIS_DETECTED");
        crisisNotification.put("alertId", alert.getId());
        crisisNotification.put("userId", alert.getUser() != null ? alert.getUser().getId() : null);
        crisisNotification.put("username", alert.getUser() != null ? alert.getUser().getUsername() : "Unknown");
        crisisNotification.put("severityLevel", alert.getSeverityLevel().name());
        crisisNotification.put("alertTitle", alert.getAlertTitle());
        crisisNotification.put("alertMessage", alert.getAlertMessage());
        crisisNotification.put("triggerSource", alert.getTriggerSource());
        crisisNotification.put("timestamp", LocalDateTime.now());
        crisisNotification.put("requiresImmediateAction", alert.requiresImmediateAttention());

        // Send to crisis monitoring topic
        sendToTopic("/topic/crisis.alerts", crisisNotification);

        // Also send to all professionals
        sendToAllProfessionals("/crisis", crisisNotification);

        logger.warn("CRISIS NOTIFICATION SENT - Alert ID: {}, User: {}, Severity: {}",
                alert.getId(),
                alert.getUser() != null ? alert.getUser().getUsername() : "N/A",
                alert.getSeverityLevel());
    }

    @Override
    public void notifyHighRiskPrediction(User user, Double riskScore, Map<String, Object> details) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "HIGH_RISK_PREDICTION");
        notification.put("userId", user.getId());
        notification.put("username", user.getUsername());
        notification.put("riskScore", riskScore);
        notification.put("details", details);
        notification.put("timestamp", LocalDateTime.now());

        sendToAllProfessionals("/high-risk", notification);
        logger.warn("High-risk prediction notification sent for user: {}, score: {}", user.getUsername(), riskScore);
    }

    @Override
    public void notifyAssignedProfessional(SystemAlert alert) {
        if (alert.getAssignedTo() == null || alert.getAssignedTo().isEmpty()) {
            logger.warn("Cannot notify assigned professional - no one assigned to alert {}", alert.getId());
            return;
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ALERT_ASSIGNED");
        notification.put("alertId", alert.getId());
        notification.put("alertTitle", alert.getAlertTitle());
        notification.put("severityLevel", alert.getSeverityLevel().name());
        notification.put("userId", alert.getUser() != null ? alert.getUser().getId() : null);
        notification.put("timestamp", LocalDateTime.now());

        sendToProfessional(alert.getAssignedTo(), "/queue/assigned-alerts", notification);
        logger.info("Notified professional {} of assigned alert {}", alert.getAssignedTo(), alert.getId());
    }

    // ==================== BROADCAST NOTIFICATIONS ====================

    @Override
    public void broadcastToAll(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend("/topic" + destination, payload);
            logger.debug("Broadcasted to all users at {}", destination);
        } catch (Exception e) {
            logger.error("Failed to broadcast: {}", e.getMessage());
        }
    }

    @Override
    public void broadcastSystemAnnouncement(String title, String message) {
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("type", "SYSTEM_ANNOUNCEMENT");
        announcement.put("title", title);
        announcement.put("message", message);
        announcement.put("timestamp", LocalDateTime.now());

        broadcastToAll("/announcements", announcement);
        logger.info("System announcement broadcasted: {}", title);
    }

    @Override
    public void broadcastToCluster(String clusterIdentifier, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend("/topic/cluster/" + clusterIdentifier + destination, payload);
            logger.debug("Broadcasted to cluster {} at {}", clusterIdentifier, destination);
        } catch (Exception e) {
            logger.error("Failed to broadcast to cluster {}: {}", clusterIdentifier, e.getMessage());
        }
    }

    // ==================== TOPIC NOTIFICATIONS ====================

    @Override
    public void sendToTopic(String topic, Object payload) {
        try {
            messagingTemplate.convertAndSend(topic, payload);
            logger.debug("Sent to topic: {}", topic);
        } catch (Exception e) {
            logger.error("Failed to send to topic {}: {}", topic, e.getMessage());
        }
    }

    @Override
    public void notifyCrisisMonitoring(Map<String, Object> crisisData) {
        crisisData.put("timestamp", LocalDateTime.now());
        sendToTopic("/topic/crisis.monitoring", crisisData);
    }

    @Override
    public void notifyAdminDashboard(String eventType, Map<String, Object> data) {
        Map<String, Object> dashboardEvent = new HashMap<>();
        dashboardEvent.put("eventType", eventType);
        dashboardEvent.put("data", data);
        dashboardEvent.put("timestamp", LocalDateTime.now());

        sendToTopic("/topic/admin.dashboard", dashboardEvent);
        logger.debug("Admin dashboard notified: {}", eventType);
    }
}