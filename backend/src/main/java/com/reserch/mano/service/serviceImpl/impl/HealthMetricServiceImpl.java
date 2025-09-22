package com.reserch.mano.service.serviceImpl.impl;

import com.reserch.mano.controller.dto.request.CreateHealthMetricRequest;
import com.reserch.mano.controller.dto.request.HealthMetricDto;
import com.reserch.mano.model.HealthMetric;
import com.reserch.mano.model.User;
import com.reserch.mano.repository.HealthMetricRepository;
import com.reserch.mano.service.serviceImpl.HealthMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for health metric-related operations.
 */
@Service
@RequiredArgsConstructor
public class HealthMetricServiceImpl implements HealthMetricService {

    private HealthMetricRepository healthMetricRepository;

    @Override
    public HealthMetricDto createHealthMetric(CreateHealthMetricRequest request, User user) {
        HealthMetric healthMetric = HealthMetric.builder()
                .user(user)
                .metricType(request.getMetricType())
                .metricValue(request.getMetricValue())
                .build();

        HealthMetric savedMetric = healthMetricRepository.save(healthMetric);

        return mapToDto(savedMetric);
    }

    @Override
    public List<HealthMetricDto> getHealthMetricsForUser(User user) {
        return healthMetricRepository.findAll().stream()
                .filter(metric -> metric.getUser().getId().equals(user.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private HealthMetricDto mapToDto(HealthMetric healthMetric) {
        return HealthMetricDto.builder()
                .id(healthMetric.getId())
                .metricType(healthMetric.getMetricType())
                .metricValue(healthMetric.getMetricValue())
                .recordedAt(healthMetric.getRecordedAt())
                .userId(healthMetric.getUser().getId())
                .build();
    }
}