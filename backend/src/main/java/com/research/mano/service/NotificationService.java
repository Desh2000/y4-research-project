package com.research.mano.service;

import com.research.mano.entity.SystemAlert;
import com.research.mano.entity.User;

import java.util.Map;

/**
 * Notification Service Interface
 * Handles real-time notifications via WebSocket and other channels
 */
public interface NotificationService {

    // ==================== USER NOTIFICATIONS ====================

    /**
     * Send notification to a specific user
     */
    void sendToUser(Long userId, String destination, Object payload);

    /**
     * Send notification to a specific user by username
     */
    void sendToUser(String username, String destination, Object payload);

    /**
     * Notify user of new prediction results
     */
    void notifyPredictionResult(Long userId, Map<String, Object> predictionData);

    /**
     * Notify user of cluster assignment change
     */
    void notifyClusterChange(Long userId, String oldCluster, String newCluster);

    /**
     * Notify user of new intervention recommendation
     */
    void notifyInterventionRecommendation(Long userId, Map<String, Object> interventionData);

    /**
     * Notify user of chat response (for mobile push)
     */
    void notifyChatResponse(Long userId, String message, Long conversationId);

    // ==================== PROFESSIONAL NOTIFICATIONS ====================

    /**
     * Send notification to all healthcare professionals
     */
    void sendToAllProfessionals(String destination, Object payload);

    /**
     * Send notification to specific professional
     */
    void sendToProfessional(String professionalId, String destination, Object payload);

    /**
     * Notify professionals of crisis detection
     */
    void notifyCrisisDetected(SystemAlert alert);

    /**
     * Notify professionals of high-risk prediction
     */
    void notifyHighRiskPrediction(User user, Double riskScore, Map<String, Object> details);

    /**
     * Notify assigned professional of new alert
     */
    void notifyAssignedProfessional(SystemAlert alert);

    // ==================== BROADCAST NOTIFICATIONS ====================

    /**
     * Broadcast to all connected users
     */
    void broadcastToAll(String destination, Object payload);

    /**
     * Broadcast system announcement
     */
    void broadcastSystemAnnouncement(String title, String message);

    /**
     * Broadcast to users in a specific cluster
     */
    void broadcastToCluster(String clusterIdentifier, String destination, Object payload);

    // ==================== TOPIC NOTIFICATIONS ====================

    /**
     * Send to a specific topic
     */
    void sendToTopic(String topic, Object payload);

    /**
     * Notify crisis monitoring topic
     */
    void notifyCrisisMonitoring(Map<String, Object> crisisData);

    /**
     * Notify admin dashboard
     */
    void notifyAdminDashboard(String eventType, Map<String, Object> data);
}