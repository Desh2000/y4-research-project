package com.reserch.mano.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for representing a health metric in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetricDto {
    private Long id;
    private String metricType;
    private double metricValue;
    private LocalDateTime recordedAt;
    private Long userId;
}
