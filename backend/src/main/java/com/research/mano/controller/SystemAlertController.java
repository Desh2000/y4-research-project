package com.research.mano.controller;

import com.research.mano.controller.request.AlertAssignmentRequest;
import com.research.mano.controller.request.AlertResolutionRequest;
import com.research.mano.controller.responce.SystemAlertDTO;
import com.research.mano.entity.SystemAlert;
import com.research.mano.entity.User;
import com.research.mano.exception.AlertNotFoundException;
import com.research.mano.exception.UserNotFoundException;
import com.research.mano.exception.ValidationException;
import com.research.mano.service.Impl.CustomUserDetailsService;
import com.research.mano.service.SystemAlertService;
import com.research.mano.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * System Alert Controller
 * Handles crisis detection, interventions, and system monitoring REST API endpoints
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SystemAlertController {

    private final SystemAlertService systemAlertService;
    private final UserService userService;

    public SystemAlertController(SystemAlertService systemAlertService, UserService userService) {
        this.systemAlertService = systemAlertService;
        this.userService = userService;
    }

    /**
     * GET /api/alerts
     * Get all alerts (Healthcare Professional/Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<Page<SystemAlertDTO>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String severityLevel,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) Boolean isResolved) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        List<SystemAlert> alerts;

        if (severityLevel != null) {
            try {
                SystemAlert.SeverityLevel severity = SystemAlert.SeverityLevel.valueOf(severityLevel.toUpperCase());
                alerts = systemAlertService.getAlertsBySeverity(severity);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("severityLevel", "Invalid severity level: " + severityLevel);
            }
        } else if (alertType != null) {
            try {
                SystemAlert.AlertType type = SystemAlert.AlertType.valueOf(alertType.toUpperCase());
                alerts = systemAlertService.getAlertsByType(type);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("alertType", "Invalid alert type: " + alertType);
            }
        } else if (Boolean.FALSE.equals(isResolved)) {
            alerts = systemAlertService.getUnresolvedAlerts();
        } else {
            alerts = systemAlertService.findAll();
        }

        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, alertDTOs.size());
        List<SystemAlertDTO> pageContent = start < alertDTOs.size() ?
                alertDTOs.subList(start, end) : List.of();

        Page<SystemAlertDTO> pageResult = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, alertDTOs.size()
        );

        return ResponseEntity.ok(pageResult);
    }

    /**
     * GET /api/alerts/{id}
     * Get alert by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<SystemAlertDTO> getAlertById(@PathVariable Long id) {
        SystemAlert alert = systemAlertService.findById(id)
                .orElseThrow(() -> new AlertNotFoundException(id));

        return ResponseEntity.ok(convertToDTO(alert));
    }

    /**
     * GET /api/alerts/my-alerts
     * Get alerts for current user
     */
    @GetMapping("/my-alerts")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<SystemAlertDTO>> getMyAlerts(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        List<SystemAlert> alerts = systemAlertService.getAlertsByUser(user);
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * GET /api/alerts/my-alerts/unresolved
     * Get unresolved alerts for current user
     */
    @GetMapping("/my-alerts/unresolved")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<SystemAlertDTO>> getMyUnresolvedAlerts(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        List<SystemAlert> alerts = systemAlertService.getUnresolvedAlertsForUser(user);
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * GET /api/alerts/crisis
     * Get crisis alerts (Healthcare Professional/Admin only)
     */
    @GetMapping("/crisis")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<SystemAlertDTO>> getCrisisAlerts() {
        List<SystemAlert> alerts = systemAlertService.getCrisisAlerts();
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * GET /api/alerts/critical
     * Get critical alerts (Healthcare Professional/Admin only)
     */
    @GetMapping("/critical")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<SystemAlertDTO>> getCriticalAlerts() {
        List<SystemAlert> alerts = systemAlertService.getCriticalAlerts();
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * GET /api/alerts/assigned-to-me
     * Get alerts assigned to current user (Healthcare Professional/Admin only)
     */
    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<SystemAlertDTO>> getAlertsAssignedToMe(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        String assignedTo = userPrincipal.getUsername();

        List<SystemAlert> alerts = systemAlertService.getAlertsAssignedTo(assignedTo);
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * GET /api/alerts/overdue
     * Get overdue alerts (Healthcare Professional/Admin only)
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<SystemAlertDTO>> getOverdueAlerts(
            @RequestParam(defaultValue = "24") int hoursOverdue) {

        List<SystemAlert> alerts = systemAlertService.getOverdueAlerts(hoursOverdue);
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * GET /api/alerts/recent-high-priority
     * Get recent high-priority alerts (Healthcare Professional/Admin only)
     */
    @GetMapping("/recent-high-priority")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<SystemAlertDTO>> getRecentHighPriorityAlerts(
            @RequestParam(defaultValue = "24") int hoursBack) {

        List<SystemAlert> alerts = systemAlertService.getRecentHighPriorityAlerts(hoursBack);
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * GET /api/alerts/date-range
     * Get alerts within date range (Healthcare Professional/Admin only)
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<SystemAlertDTO>> getAlertsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<SystemAlert> alerts = systemAlertService.getAlertsByDateRange(startDate, endDate);
        List<SystemAlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    /**
     * PUT /api/alerts/{id}/resolve
     * Resolve alert (Healthcare Professional/Admin only)
     */
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<SystemAlertDTO> resolveAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertResolutionRequest resolutionRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        String resolvedBy = resolutionRequest.getResolvedBy() != null ?
                resolutionRequest.getResolvedBy() : userPrincipal.getUsername();

        SystemAlert resolvedAlert = systemAlertService.resolveAlert(
                id, resolvedBy, resolutionRequest.getResolutionNotes(), resolutionRequest.getActionTaken()
        );

        return ResponseEntity.ok(convertToDTO(resolvedAlert));
    }

    /**
     * PUT /api/alerts/{id}/assign
     * Assign alert to professional (Healthcare Professional/Admin only)
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<SystemAlertDTO> assignAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertAssignmentRequest assignmentRequest) {

        SystemAlert assignedAlert = systemAlertService.assignToProfessional(id, assignmentRequest.getAssignedTo());
        return ResponseEntity.ok(convertToDTO(assignedAlert));
    }

    /**
     * PUT /api/alerts/{id}/escalate
     * Escalate alert severity (Healthcare Professional/Admin only)
     */
    @PutMapping("/{id}/escalate")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<SystemAlertDTO> escalateAlert(
            @PathVariable Long id,
            @RequestParam String newSeverity,
            @RequestParam String reason) {

        try {
            SystemAlert.SeverityLevel severityLevel = SystemAlert.SeverityLevel.valueOf(newSeverity.toUpperCase());
            SystemAlert escalatedAlert = systemAlertService.escalateAlert(id, severityLevel, reason);
            return ResponseEntity.ok(convertToDTO(escalatedAlert));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("newSeverity", "Invalid severity level: " + newSeverity);
        }
    }

    /**
     * PUT /api/alerts/{id}/update-action
     * Update action taken for alert (Healthcare Professional/Admin only)
     */
    @PutMapping("/{id}/update-action")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<SystemAlertDTO> updateActionTaken(
            @PathVariable Long id,
            @RequestParam String actionTaken) {

        SystemAlert updatedAlert = systemAlertService.updateActionTaken(id, actionTaken);
        return ResponseEntity.ok(convertToDTO(updatedAlert));
    }

    /**
     * GET /api/alerts/statistics/counts-by-type
     * Get alert counts by type (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/counts-by-type")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getAlertCountsByType() {
        List<Object[]> counts = systemAlertService.countAlertsByType();
        return ResponseEntity.ok(counts);
    }

    /**
     * GET /api/alerts/statistics/counts-by-severity
     * Get alert counts by severity (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/counts-by-severity")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getAlertCountsBySeverity() {
        List<Object[]> counts = systemAlertService.countAlertsBySeverity();
        return ResponseEntity.ok(counts);
    }

    /**
     * GET /api/alerts/statistics/daily-counts
     * Get daily alert counts (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/daily-counts")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getDailyAlertCounts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        List<Object[]> counts = systemAlertService.getDailyAlertCounts(startDate);
        return ResponseEntity.ok(counts);
    }

    /**
     * GET /api/alerts/statistics/resolution-stats
     * Get alert resolution statistics (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/resolution-stats")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<Object[]> getAlertResolutionStats(
            @RequestParam(defaultValue = "30") int daysBack) {

        Object[] stats = systemAlertService.getAlertResolutionStats(daysBack);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/alerts/statistics/frequent-users
     * Get most frequently alerted users (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/frequent-users")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getMostFrequentlyAlertedUsers() {
        List<Object[]> users = systemAlertService.getMostFrequentlyAlertedUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/alerts/statistics/risk-trend
     * Get user risk trend based on alerts (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/risk-trend")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getUserRiskTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        List<Object[]> trend = systemAlertService.getUserRiskTrend(startDate);
        return ResponseEntity.ok(trend);
    }

    /**
     * GET /api/alerts/statistics/summary-report
     * Generate alert summary report (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/summary-report")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<String> generateAlertSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String report = systemAlertService.generateAlertSummaryReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/alerts/statistics/unresolved-by-severity
     * Get unresolved alert counts by severity (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/unresolved-by-severity")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getUnresolvedAlertsBySeverity() {
        List<Object[]> counts = systemAlertService.countUnresolvedAlertsBySeverity();
        return ResponseEntity.ok(counts);
    }

    /**
     * POST /api/alerts/auto-resolve-system
     * Auto-resolve system alerts (Admin only)
     */
    @PostMapping("/auto-resolve-system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> autoResolveSystemAlerts() {
        systemAlertService.autoResolveSystemAlerts();
        return ResponseEntity.ok(new ApiResponse(true, "System alerts auto-resolved successfully"));
    }

    /**
     * POST /api/alerts/cleanup-resolved
     * Clean up old resolved alerts (Admin only)
     */
    @PostMapping("/cleanup-resolved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> cleanupResolvedAlerts(
            @RequestParam(defaultValue = "90") int daysOld) {

        systemAlertService.cleanupResolvedAlerts(daysOld);
        return ResponseEntity.ok(new ApiResponse(true, "Resolved alerts cleaned up successfully"));
    }

    /**
     * GET /api/alerts/user/{userId}/patterns
     * Check for alert patterns for specific user (Healthcare Professional/Admin only)
     */
    @GetMapping("/user/{userId}/patterns")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<SystemAlertDTO>> checkUserAlertPatterns(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int daysBack) {

        User user = getUserById(userId);
        List<SystemAlert> patterns = systemAlertService.checkForAlertPatterns(user, daysBack);
        List<SystemAlertDTO> patternDTOs = patterns.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(patternDTOs);
    }

    // Helper methods
    private CustomUserDetailsService.UserPrincipal getCurrentUser(Authentication authentication) {
        return (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
    }

    private User getUserById(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private SystemAlertDTO convertToDTO(SystemAlert alert) {
        SystemAlertDTO dto = new SystemAlertDTO();
        dto.setId(alert.getId());
        if (alert.getUser() != null) {
            dto.setUserId(alert.getUser().getId());
            dto.setUsername(alert.getUser().getUsername());
        }
        dto.setAlertType(alert.getAlertType().name());
        dto.setSeverityLevel(alert.getSeverityLevel().name());
        dto.setTitle(alert.getAlertTitle());
        dto.setMessage(alert.getAlertMessage());
        dto.setTriggerSource(alert.getTriggerSource());
        dto.setTriggerData(alert.getTriggerData());
        dto.setIsCrisis(alert.getIsCrisis());
        dto.setIsResolved(alert.getIsResolved());
        dto.setAssignedTo(alert.getAssignedTo());
        dto.setEmergencyContactNotified(alert.getEmergencyContactNotified());
        dto.setProfessionalNotified(alert.getProfessionalNotified());
        dto.setActionTaken(alert.getActionTaken());
        dto.setResolvedBy(alert.getResolvedBy());
        dto.setResolutionNotes(alert.getResolutionNotes());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setNotificationSentAt(alert.getNotificationSentAt());

        return dto;
    }

    /**
     * Generic API Response class
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ApiResponse {
        private Boolean success;
        private String message;
    }
}
