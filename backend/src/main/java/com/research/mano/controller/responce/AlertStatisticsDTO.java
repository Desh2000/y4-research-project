package com.research.mano.controller.responce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertStatisticsDTO {
    private Long totalAlerts;
    private Long resolvedAlerts;
    private Long crisisAlerts;
    private Long criticalAlerts;
    private Double resolutionRate;
    private Double averageResolutionHours;
    private Object[] alertsByType;
    private Object[] alertsBySeverity;
}
