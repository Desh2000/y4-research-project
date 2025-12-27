package com.research.mano.controller;

import com.research.mano.controller.request.*;
import com.research.mano.controller.responce.*;
import com.research.mano.entity.User;
import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.service.MentalHealthPredictionService;
import com.research.mano.service.UserService;
import com.research.mano.service.Impl.CustomUserDetailsService;
import com.research.mano.exception.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Mental Health Prediction Controller
 * Handles Component 2 (LSTM Prediction System) REST API endpoints
 */
@RestController
@RequestMapping("/api/mental-health/predictions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MentalHealthPredictionController {

    @Autowired
    private MentalHealthPredictionService predictionService;

    @Autowired
    private UserService userService;

    /**
     * POST /api/mental-health/predictions
     * Create a new mental health prediction
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<MentalHealthPredictionDTO> createPrediction(
            @Valid @RequestBody MentalHealthPredictionDTO predictionRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        MentalHealthPrediction prediction = predictionService.createPrediction(
                user,
                predictionRequest.getStressScore(),
                predictionRequest.getDepressionScore(),
                predictionRequest.getAnxietyScore(),
                predictionRequest.getModelVersion() != null ? predictionRequest.getModelVersion() : "1.0.0",
                predictionRequest.getDataSource() != null ? predictionRequest.getDataSource() : "MANUAL_INPUT"
        );

        return ResponseEntity.ok(convertToDTO(prediction));
    }

    /**
     * GET /api/mental-health/predictions
     * Get current user's predictions with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Page<MentalHealthPredictionDTO>> getUserPredictions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "predictionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        List<MentalHealthPrediction> predictions = predictionService.getAllPredictions(user);
        List<MentalHealthPredictionDTO> predictionDTOs = predictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Manual pagination (in real scenario, use repository pagination)
        int start = page * size;
        int end = Math.min(start + size, predictionDTOs.size());
        List<MentalHealthPredictionDTO> pageContent = start < predictionDTOs.size() ?
                predictionDTOs.subList(start, end) : List.of();

        Page<MentalHealthPredictionDTO> pageResult = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, predictionDTOs.size()
        );

        return ResponseEntity.ok(pageResult);
    }

    /**
     * GET /api/mental-health/predictions/latest
     * Get user's latest prediction
     */
    @GetMapping("/latest")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<MentalHealthPredictionDTO> getLatestPrediction(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        MentalHealthPrediction prediction = predictionService.getLatestPrediction(user)
                .orElseThrow(() -> new PredictionNotFoundException(0L));

        return ResponseEntity.ok(convertToDTO(prediction));
    }

    /**
     * GET /api/mental-health/predictions/{id}
     * Get specific prediction by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<MentalHealthPredictionDTO> getPredictionById(
            @PathVariable Long id,
            Authentication authentication) {

        MentalHealthPrediction prediction = predictionService.findById(id)
                .orElseThrow(() -> new PredictionNotFoundException(id));

        // Check ownership
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        if (!prediction.getUser().getId().equals(userPrincipal.getId()) &&
                !hasRole(authentication, "HEALTHCARE_PROFESSIONAL") &&
                !hasRole(authentication, "ADMIN")) {
            throw new UnauthorizedAccessException("prediction");
        }

        return ResponseEntity.ok(convertToDTO(prediction));
    }

    /**
     * PUT /api/mental-health/predictions/{id}
     * Update prediction scores (for corrections)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<MentalHealthPredictionDTO> updatePrediction(
            @PathVariable Long id,
            @Valid @RequestBody MentalHealthScoreUpdateRequest updateRequest) {

        MentalHealthPrediction updatedPrediction = predictionService.updatePredictionScores(
                id,
                updateRequest.getStressScore(),
                updateRequest.getDepressionScore(),
                updateRequest.getAnxietyScore()
        );

        return ResponseEntity.ok(convertToDTO(updatedPrediction));
    }

    /**
     * GET /api/mental-health/predictions/date-range
     * Get predictions within date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<MentalHealthPredictionDTO>> getPredictionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        List<MentalHealthPrediction> predictions = predictionService.getPredictionsByDateRange(
                user, startDate, endDate
        );

        List<MentalHealthPredictionDTO> predictionDTOs = predictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(predictionDTOs);
    }

    /**
     * GET /api/mental-health/predictions/risk-progression
     * Get user's risk progression over time
     */
    @GetMapping("/risk-progression")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<Object[]>> getRiskProgression(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);

        List<Object[]> progression = predictionService.getUserRiskProgression(userPrincipal.getId());
        return ResponseEntity.ok(progression);
    }

    /**
     * GET /api/mental-health/predictions/high-risk
     * Get high-risk predictions (Admin/Healthcare Professional only)
     */
    @GetMapping("/high-risk")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<MentalHealthPredictionDTO>> getHighRiskPredictions() {
        List<MentalHealthPrediction> predictions = predictionService.findHighRiskPredictions();

        List<MentalHealthPredictionDTO> predictionDTOs = predictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(predictionDTOs);
    }

    /**
     * GET /api/mental-health/predictions/cluster/{category}
     * Get predictions by cluster category
     */
    @GetMapping("/cluster/{category}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<MentalHealthPredictionDTO>> getPredictionsByCluster(
            @PathVariable String category) {

        try {
            MentalHealthPrediction.ClusterCategory clusterCategory =
                    MentalHealthPrediction.ClusterCategory.valueOf(category.toUpperCase());

            List<MentalHealthPrediction> predictions = predictionService.findByClusterCategory(clusterCategory);

            List<MentalHealthPredictionDTO> predictionDTOs = predictions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(predictionDTOs);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("category", "Invalid cluster category: " + category);
        }
    }

    /**
     * GET /api/mental-health/predictions/trending-risks
     * Get predictions with increasing risk trends
     */
    @GetMapping("/trending-risks")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<MentalHealthPredictionDTO>> getTrendingRiskIncreases() {
        List<MentalHealthPrediction> predictions = predictionService.findTrendingRiskIncreases();

        List<MentalHealthPredictionDTO> predictionDTOs = predictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(predictionDTOs);
    }

    /**
     * GET /api/mental-health/predictions/statistics
     * Get prediction statistics (Admin/Healthcare Professional only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<Object> getPredictionStatistics() {
        List<Object[]> clusterStats = predictionService.getPredictionStatsByCluster();
        List<Object[]> dailyCounts = predictionService.getDailyPredictionCounts(
                LocalDateTime.now().minusDays(30)
        );
        List<Object[]> averageScores = predictionService.getAverageScoresByDate(
                LocalDateTime.now().minusDays(30)
        );

        return ResponseEntity.ok(new Object() {
            public final List<Object[]> clusterStatistics = clusterStats;
            public final List<Object[]> dailyPredictionCounts = dailyCounts;
            public final List<Object[]> averageScoresTrend = averageScores;
        });
    }

    /**
     * POST /api/mental-health/predictions/batch-clustering
     * Process new predictions for clustering (Admin only)
     */
    @PostMapping("/batch-clustering")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MentalHealthPredictionDTO>> processNewPredictionsForClustering() {
        List<MentalHealthPrediction> processedPredictions = predictionService.processNewPredictionsForClustering();

        List<MentalHealthPredictionDTO> predictionDTOs = processedPredictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(predictionDTOs);
    }

    /**
     * DELETE /api/mental-health/predictions/{id}
     * Delete prediction (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePrediction(@PathVariable Long id) {
        predictionService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Helper methods
    private CustomUserDetailsService.UserPrincipal getCurrentUser(Authentication authentication) {
        return (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
    }

    private User getUserById(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    private MentalHealthPredictionDTO convertToDTO(MentalHealthPrediction prediction) {
        MentalHealthPredictionDTO dto = new MentalHealthPredictionDTO();
        dto.setId(prediction.getId());
        dto.setUserId(prediction.getUser().getId());
        dto.setUsername(prediction.getUser().getUsername());
        dto.setStressScore(prediction.getStressScore());
        dto.setDepressionScore(prediction.getDepressionScore());
        dto.setAnxietyScore(prediction.getAnxietyScore());
        dto.setOverallRiskScore(prediction.getOverallRiskScore());
        dto.setPredictionDate(prediction.getPredictionDate());
        dto.setModelVersion(prediction.getModelVersion());
        dto.setDataSource(prediction.getDataSource());

        if (prediction.getPrimaryClusterCategory() != null) {
            dto.setPrimaryClusterCategory(prediction.getPrimaryClusterCategory().name());
        }
        if (prediction.getPrimaryClusterLevel() != null) {
            dto.setPrimaryClusterLevel(prediction.getPrimaryClusterLevel().name());
        }
        dto.setClusterAssignmentDate(prediction.getClusterAssignmentDate());

        return dto;
    }
}