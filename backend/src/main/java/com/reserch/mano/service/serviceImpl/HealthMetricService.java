package com.reserch.mano.service.serviceImpl;

import com.reserch.mano.controller.dto.request.CreateHealthMetricRequest;
import com.reserch.mano.controller.dto.request.HealthMetricDto;
import com.reserch.mano.model.User;

import java.util.List;

/**
 * Service interface for health metric-related operations.
 */
public interface HealthMetricService {
    HealthMetricDto createHealthMetric(CreateHealthMetricRequest request, User user);
    List<HealthMetricDto> getHealthMetricsForUser(User user);
}
