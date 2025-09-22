package com.reserch.mano.controller.dto;

import com.reserch.mano.controller.dto.request.CreateHealthMetricRequest;
import com.reserch.mano.controller.dto.request.HealthMetricDto;
import com.reserch.mano.model.User;
import com.reserch.mano.service.serviceImpl.HealthMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling health metric-related API requests.
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class HealthMetricController {

    private final HealthMetricService healthMetricService;

    /**
     * Endpoint to create a new health metric for the authenticated user.
     * @param request The request body containing metric details.
     * @param user The currently authenticated user, injected by Spring Security.
     * @return The created health metric DTO.
     */
    @PostMapping
    public ResponseEntity<HealthMetricDto> createHealthMetric(@RequestBody CreateHealthMetricRequest request, @AuthenticationPrincipal User user) {
        HealthMetricDto createdMetric = healthMetricService.createHealthMetric(request, user);
        return new ResponseEntity<>(createdMetric, HttpStatus.CREATED);
    }

    /**
     * Endpoint to get all health metrics for the authenticated user.
     * @param user The currently authenticated user, injected by Spring Security.
     * @return A list of health metric DTOs.
     */
    @GetMapping
    public ResponseEntity<List<HealthMetricDto>> getHealthMetricsForUser(@AuthenticationPrincipal User user) {
        List<HealthMetricDto> metrics = healthMetricService.getHealthMetricsForUser(user);
        return ResponseEntity.ok(metrics);
    }
}