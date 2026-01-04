package com.research.mano.controller.responce;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemAlertDTO {
    private Long id;
    private Long userId;
    private String username;
    private String alertType;
    private String severityLevel;
    private String title;
    private String message;
    private String triggerSource;
    private String triggerData;
    private Boolean isCrisis;
    private Boolean isResolved;
    private String assignedTo;
    private Boolean emergencyContactNotified;
    private Boolean professionalNotified;
    private String actionTaken;
    private String resolvedBy;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime notificationSentAt;
}
