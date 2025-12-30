package com.research.mano.repository;

import com.research.mano.entity.SystemAlert;
import com.research.mano.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * System Alert Repository Interface
 * Handles CRUD operations for system alerts and crisis interventions
 */
@Repository
public interface SystemAlertRepository extends BaseRepository<SystemAlert, Long> {

    /**
     * Find alerts by user
     */
    List<SystemAlert> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find alerts by alert type
     */
    List<SystemAlert> findByAlertType(SystemAlert.AlertType alertType);

    /**
     * Find alerts by severity level
     */
    List<SystemAlert> findBySeverityLevel(SystemAlert.SeverityLevel severityLevel);

    /**
     * Find unresolved alerts
     */
    List<SystemAlert> findByIsResolvedFalseOrderByCreatedAtDesc();

    /**
     * Find resolved alerts
     */
    List<SystemAlert> findByIsResolvedTrueOrderByResolvedAtDesc();

    /**
     * Find crisis alerts
     */
    List<SystemAlert> findByIsCrisisTrueOrderByCreatedAtDesc();

    /**
     * Find critical alerts (immediate attention required)
     */
    List<SystemAlert> findBySeverityLevelOrderByCreatedAtDesc(SystemAlert.SeverityLevel severityLevel);

    /**
     * Find alerts assigned to specific professional
     */
    List<SystemAlert> findByAssignedToOrderByCreatedAtDesc(String assignedTo);

    /**
     * Find alerts by trigger source
     */
    List<SystemAlert> findByTriggerSource(String triggerSource);

    /**
     * Find unresolved alerts for a user
     */
    List<SystemAlert> findByUserAndIsResolvedFalseOrderByCreatedAtDesc(User user);

    /**
     * Find alerts within date range
     */
    @Query("SELECT sa FROM SystemAlert sa WHERE sa.createdAt BETWEEN :startDate AND :endDate ORDER BY sa.createdAt DESC")
    List<SystemAlert> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find alerts requiring emergency contact notification
     */
    @Query("SELECT sa FROM SystemAlert sa WHERE sa.isCrisis = true AND sa.emergencyContactNotified = false")
    List<SystemAlert> findAlertsNeedingEmergencyNotification();

    /**
     * Find alerts requiring professional notification
     */
    @Query("SELECT sa FROM SystemAlert sa WHERE sa.severityLevel IN ('HIGH', 'CRITICAL') AND sa.professionalNotified = false")
    List<SystemAlert> findAlertsNeedingProfessionalNotification();

    /**
     * Find overdue alerts (unresolved for too long)
     */
    @Query("SELECT sa FROM SystemAlert sa WHERE sa.isResolved = false AND sa.createdAt < :overdueThreshold")
    List<SystemAlert> findOverdueAlerts(@Param("overdueThreshold") LocalDateTime overdueThreshold);

    /**
     * Count alerts by type
     */
    @Query("SELECT sa.alertType, COUNT(sa) FROM SystemAlert sa GROUP BY sa.alertType")
    List<Object[]> countAlertsByType();

    /**
     * Count alerts by severity
     */
    @Query("SELECT sa.severityLevel, COUNT(sa) FROM SystemAlert sa GROUP BY sa.severityLevel")
    List<Object[]> countAlertsBySeverity();

    /**
     * Get daily alert counts
     */
    @Query("SELECT DATE(sa.createdAt), COUNT(sa) FROM SystemAlert sa " +
            "WHERE sa.createdAt >= :startDate GROUP BY DATE(sa.createdAt)")
    List<Object[]> getDailyAlertCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Get alert resolution statistics
     */
    @Query("SELECT " +
            "COUNT(sa) as total, " +
            "SUM(CASE WHEN sa.isResolved = true THEN 1 ELSE 0 END) as resolved, " +
            "AVG(CASE WHEN sa.isResolved = true THEN TIMESTAMPDIFF(HOUR, sa.createdAt, sa.resolvedAt) ELSE NULL END) as avgResolutionHours " +
            "FROM SystemAlert sa WHERE sa.createdAt >= :since")
    Object[] getAlertResolutionStats(@Param("since") LocalDateTime since);

    /**
     * Find most frequently alerted users
     */
    @Query("SELECT sa.user, COUNT(sa) FROM SystemAlert sa WHERE sa.user IS NOT NULL " +
            "GROUP BY sa.user ORDER BY COUNT(sa) DESC")
    List<Object[]> getMostFrequentlyAlertedUsers();

    /**
     * Mark alert as resolved
     */
    @Modifying
    @Query("UPDATE SystemAlert sa SET sa.isResolved = true, sa.resolvedAt = :resolvedAt, " +
            "sa.resolvedBy = :resolvedBy, sa.resolutionNotes = :notes WHERE sa.id = :alertId")
    void markAsResolved(@Param("alertId") Long alertId,
                        @Param("resolvedAt") LocalDateTime resolvedAt,
                        @Param("resolvedBy") String resolvedBy,
                        @Param("notes") String notes);

    /**
     * Mark emergency contact as notified
     */
    @Modifying
    @Query("UPDATE SystemAlert sa SET sa.emergencyContactNotified = true, sa.notificationSentAt = :notificationTime " +
            "WHERE sa.id = :alertId")
    void markEmergencyContactNotified(@Param("alertId") Long alertId, @Param("notificationTime") LocalDateTime notificationTime);

    /**
     * Mark professional as notified
     */
    @Modifying
    @Query("UPDATE SystemAlert sa SET sa.professionalNotified = true, sa.notificationSentAt = :notificationTime " +
            "WHERE sa.id = :alertId")
    void markProfessionalNotified(@Param("alertId") Long alertId, @Param("notificationTime") LocalDateTime notificationTime);

    /**
     * Assign alert to professional
     */
    @Modifying
    @Query("UPDATE SystemAlert sa SET sa.assignedTo = :assignedTo WHERE sa.id = :alertId")
    void assignToProfessional(@Param("alertId") Long alertId, @Param("assignedTo") String assignedTo);

    /**
     * Update action taken
     */
    @Modifying
    @Query("UPDATE SystemAlert sa SET sa.actionTaken = :actionTaken WHERE sa.id = :alertId")
    void updateActionTaken(@Param("alertId") Long alertId, @Param("actionTaken") String actionTaken);

    /**
     * Find alerts by component source
     */
    @Query("SELECT sa FROM SystemAlert sa WHERE sa.triggerSource IN :components")
    List<SystemAlert> findByComponentSources(@Param("components") List<String> components);

    /**
     * Find recent high-priority alerts
     */
    @Query("SELECT sa FROM SystemAlert sa WHERE sa.severityLevel IN ('HIGH', 'CRITICAL') " +
            "AND sa.createdAt >= :since ORDER BY sa.severityLevel DESC, sa.createdAt DESC")
    List<SystemAlert> findRecentHighPriorityAlerts(@Param("since") LocalDateTime since);

    /**
     * Get user risk trend based on alerts
     */
    @Query("SELECT DATE(sa.createdAt), sa.user.id, COUNT(sa) FROM SystemAlert sa " +
            "WHERE sa.user IS NOT NULL AND sa.createdAt >= :startDate " +
            "GROUP BY DATE(sa.createdAt), sa.user.id ORDER BY DATE(sa.createdAt)")
    List<Object[]> getUserRiskTrend(@Param("startDate") LocalDateTime startDate);

    /**
     * Find system-wide alerts (no specific user)
     */
    List<SystemAlert> findByUserIsNullOrderByCreatedAtDesc();

    /**
     * Count unresolved alerts by severity
     */
    @Query("SELECT sa.severityLevel, COUNT(sa) FROM SystemAlert sa WHERE sa.isResolved = false GROUP BY sa.severityLevel")
    List<Object[]> countUnresolvedAlertsBySeverity();
}