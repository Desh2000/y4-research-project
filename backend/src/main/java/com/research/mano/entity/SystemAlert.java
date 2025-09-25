package com.research.mano.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * System Alert Entity for monitoring and interventions
 * Handles crisis detection, high-risk alerts, and system notifications
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "system_alerts")
@NoArgsConstructor
@AllArgsConstructor
public class SystemAlert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Can be null for system-wide alerts

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false)
    private SeverityLevel severityLevel;

    @Column(name = "alert_title", nullable = false)
    private String alertTitle;

    @Column(name = "alert_message", columnDefinition = "TEXT", nullable = false)
    private String alertMessage;

    @Column(name = "trigger_source")
    private String triggerSource; // Component that triggered the alert

    @Column(name = "trigger_data", columnDefinition = "TEXT")
    private String triggerData; // JSON data related to the trigger

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "action_taken")
    private String actionTaken;

    @Column(name = "assigned_to")
    private String assignedTo; // Professional assigned to handle the alert

    @Column(name = "is_crisis")
    private Boolean isCrisis = false;

    @Column(name = "emergency_contact_notified")
    private Boolean emergencyContactNotified = false;

    @Column(name = "professional_notified")
    private Boolean professionalNotified = false;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    // Utility Methods
    public void markAsResolved(String resolvedBy, String notes) {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = notes;
    }

    public boolean requiresImmediateAttention() {
        return severityLevel == SeverityLevel.CRITICAL || isCrisis;
    }

    @Override
    public String toString() {
        return "SystemAlert{" +
                "id=" + getId() +
                ", userId=" + (user != null ? user.getId() : null) +
                ", type=" + alertType +
                ", severity=" + severityLevel +
                ", title='" + alertTitle + '\'' +
                ", resolved=" + isResolved +
                '}';
    }

    /**
     * Alert Type Enum
     */
    public enum AlertType {
        HIGH_RISK_PREDICTION("High risk prediction from LSTM model"),
        CRISIS_CHAT_DETECTED("Crisis keywords detected in chat"),
        CLUSTER_CHANGE("User moved to higher risk cluster"),
        MISSED_ASSESSMENT("User missed scheduled assessment"),
        INACTIVE_USER("User inactive for extended period"),
        SYSTEM_ERROR("System error occurred"),
        MODEL_DRIFT("ML model performance degradation"),
        DATA_ANOMALY("Unusual data patterns detected"),
        PRIVACY_BREACH("Potential privacy breach detected"),
        INTERVENTION_NEEDED("Professional intervention recommended");

        private final String description;

        AlertType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Severity Level Enum
     */
    public enum SeverityLevel {
        LOW("Low priority alert"),
        MEDIUM("Medium priority alert"),
        HIGH("High priority alert"),
        CRITICAL("Critical alert requiring immediate attention");

        private final String description;

        SeverityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}