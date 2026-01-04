package com.research.mano.controller;

import com.research.mano.controller.request.PredictionRequest;
import com.research.mano.controller.responce.PredictionResultDTO;
import com.research.mano.service.PredictionService;
import com.research.mano.service.Impl.CustomUserDetailsService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Prediction Controller for Component 2
 * Comprehensive REST API for mental health risk predictions
 */
@RestController
@RequestMapping("/predictions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    // ==================== CREATE PREDICTIONS ====================

    /**
     * POST /api/predictions
     * Create a new prediction with multimodal data
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> createPrediction(
            @Valid @RequestBody PredictionRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        PredictionResultDTO result = predictionService.createPrediction(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/predictions/direct
     * Create prediction with direct score input
     */
    @PostMapping("/direct")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> createDirectPrediction(
            @RequestParam Double stressScore,
            @RequestParam Double depressionScore,
            @RequestParam Double anxietyScore,
            @RequestParam(required = false) String dataSource,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        PredictionResultDTO result = predictionService.createDirectPrediction(
                userId, stressScore, depressionScore, anxietyScore, dataSource);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/predictions/questionnaire/phq9
     * Create prediction from PHQ-9 questionnaire
     */
    @PostMapping("/questionnaire/phq9")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> createFromPHQ9(
            @RequestBody List<Integer> responses,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        PredictionResultDTO result = predictionService.createFromPHQ9(userId, responses);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/predictions/questionnaire/gad7
     * Create prediction from GAD-7 questionnaire
     */
    @PostMapping("/questionnaire/gad7")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> createFromGAD7(
            @RequestBody List<Integer> responses,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        PredictionResultDTO result = predictionService.createFromGAD7(userId, responses);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/predictions/questionnaire/pss
     * Create prediction from PSS questionnaire
     */
    @PostMapping("/questionnaire/pss")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> createFromPSS(
            @RequestBody List<Integer> responses,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        PredictionResultDTO result = predictionService.createFromPSS(userId, responses);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * POST /api/predictions/questionnaire/all
     * Create prediction from all questionnaires
     */
    @PostMapping("/questionnaire/all")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> createFromAllQuestionnaires(
            @RequestBody Map<String, List<Integer>> questionnaires,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        PredictionResultDTO result = predictionService.createFromQuestionnaires(
                userId,
                questionnaires.get("phq9"),
                questionnaires.get("gad7"),
                questionnaires.get("pss")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ==================== RETRIEVE PREDICTIONS ====================

    /**
     * GET /api/predictions/{id}
     * Get prediction by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> getPredictionById(@PathVariable Long id) {
        return predictionService.getPredictionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/predictions/me/latest
     * Get current user's latest prediction
     */
    @GetMapping("/me/latest")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> getLatestPrediction(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return predictionService.getLatestPrediction(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/predictions/me/history
     * Get current user's prediction history
     */
    @GetMapping("/me/history")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<PredictionResultDTO>> getPredictionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        List<PredictionResultDTO> history = predictionService.getUserPredictionHistory(userId, page, size);

        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/predictions/me/date-range
     * Get predictions within date range
     */
    @GetMapping("/me/date-range")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<PredictionResultDTO>> getPredictionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        List<PredictionResultDTO> predictions = predictionService.getPredictionsByDateRange(userId, start, end);

        return ResponseEntity.ok(predictions);
    }

    /**
     * GET /api/predictions/user/{userId}/history
     * Get specific user's prediction history (Healthcare Professional/Admin)
     */
    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<PredictionResultDTO>> getUserPredictionHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<PredictionResultDTO> history = predictionService.getUserPredictionHistory(userId, page, size);
        return ResponseEntity.ok(history);
    }

    // ==================== ANALYSIS ENDPOINTS ====================

    /**
     * GET /api/predictions/me/progression
     * Get risk progression over time
     */
    @GetMapping("/me/progression")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getRiskProgression(
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        Map<String, Object> progression = predictionService.getRiskProgression(userId, days);

        return ResponseEntity.ok(progression);
    }

    /**
     * GET /api/predictions/me/trends
     * Get trend analysis
     */
    @GetMapping("/me/trends")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getTrendAnalysis(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Map<String, Object> trends = predictionService.getTrendAnalysis(userId);

        return ResponseEntity.ok(trends);
    }

    /**
     * GET /api/predictions/me/cluster-comparison
     * Compare scores with cluster averages
     */
    @GetMapping("/me/cluster-comparison")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getClusterComparison(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Map<String, Object> comparison = predictionService.getClusterComparison(userId);

        return ResponseEntity.ok(comparison);
    }

    /**
     * GET /api/predictions/me/statistics
     * Get prediction statistics
     */
    @GetMapping("/me/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getPredictionStatistics(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Map<String, Object> stats = predictionService.getUserPredictionStatistics(userId);

        return ResponseEntity.ok(stats);
    }

    // ==================== CLUSTER ENDPOINTS ====================

    /**
     * GET /api/predictions/cluster-assignment
     * Get cluster assignment for given scores
     */
    @GetMapping("/cluster-assignment")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getClusterAssignment(
            @RequestParam Double stressScore,
            @RequestParam Double depressionScore,
            @RequestParam Double anxietyScore) {

        Map<String, Object> assignment = predictionService.getClusterAssignment(
                stressScore, depressionScore, anxietyScore);

        return ResponseEntity.ok(assignment);
    }

    // ==================== ALERT ENDPOINTS ====================

    /**
     * GET /api/predictions/{id}/requires-attention
     * Check if prediction requires immediate attention
     */
    @GetMapping("/{id}/requires-attention")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Boolean>> checkRequiresAttention(@PathVariable Long id) {
        boolean requiresAttention = predictionService.requiresImmediateAttention(id);
        return ResponseEntity.ok(Map.of("requiresImmediateAttention", requiresAttention));
    }

    /**
     * GET /api/predictions/high-risk
     * Get all high-risk predictions (Healthcare Professional/Admin)
     */
    @GetMapping("/high-risk")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<PredictionResultDTO>> getHighRiskPredictions(
            @RequestParam(defaultValue = "0.7") Double threshold) {

        List<PredictionResultDTO> predictions = predictionService.getHighRiskPredictions(threshold);
        return ResponseEntity.ok(predictions);
    }

    /**
     * GET /api/predictions/requiring-follow-up
     * Get predictions requiring follow-up (Healthcare Professional/Admin)
     */
    @GetMapping("/requiring-follow-up")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<PredictionResultDTO>> getPredictionsRequiringFollowUp() {
        List<PredictionResultDTO> predictions = predictionService.getPredictionsRequiringFollowUp();
        return ResponseEntity.ok(predictions);
    }

    // ==================== ML SERVICE ENDPOINTS ====================

    /**
     * GET /api/predictions/ml/health
     * Check ML service health status
     */
    @GetMapping("/ml/health")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Boolean>> getMLServiceHealth() {
        Map<String, Boolean> health = predictionService.checkMLServiceHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * GET /api/predictions/ml/models
     * Get available model versions
     */
    @GetMapping("/ml/models")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<String>> getAvailableModels() {
        List<String> models = predictionService.getAvailableModelVersions();
        return ResponseEntity.ok(models);
    }

    /**
     * GET /api/predictions/ml/metrics
     * Get model performance metrics
     */
    @GetMapping("/ml/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getModelMetrics() {
        Map<String, Object> metrics = predictionService.getModelMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * POST /api/predictions/{id}/rerun
     * Rerun prediction with different model version
     */
    @PostMapping("/{id}/rerun")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<PredictionResultDTO> rerunPrediction(
            @PathVariable Long id,
            @RequestParam(required = false) String modelVersion) {

        PredictionResultDTO result = predictionService.rerunPrediction(id, modelVersion);
        return ResponseEntity.ok(result);
    }

    // ==================== HELPER METHODS ====================

    private Long getCurrentUserId(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal =
                (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}