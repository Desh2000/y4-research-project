package com.research.mano.controller;

import com.research.mano.controller.request.InterventionOutcomeRequest;
import com.research.mano.controller.request.SimulationRequest;
import com.research.mano.controller.responce.InterventionOutcomeDTO;
import com.research.mano.entity.InterventionOutcome;
import com.research.mano.entity.InterventionOutcome.OutcomeStatus;
import com.research.mano.entity.InterventionOutcome.EffectivenessRating;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.User;
import com.research.mano.service.InterventionOutcomeService;
import com.research.mano.service.UserService;
import com.research.mano.service.Impl.CustomUserDetailsService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Intervention Outcome Controller for Component 1
 * REST API endpoints for tracking intervention results and simulations
 */
@RestController
@RequestMapping("/intervention-outcomes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InterventionOutcomeController {

    @Autowired
    private InterventionOutcomeService outcomeService;

    @Autowired
    private UserService userService;

    // ==================== USER INTERVENTION MANAGEMENT ====================

    /**
     * POST /api/intervention-outcomes/start
     * Start a new intervention for the current user
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionOutcomeDTO> startIntervention(
            @Valid @RequestBody InterventionOutcomeRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        InterventionOutcome outcome = outcomeService.startIntervention(
                userId,
                request.getInterventionId(),
                request.getPreStressScore(),
                request.getPreDepressionScore(),
                request.getPreAnxietyScore()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InterventionOutcomeDTO.fromEntity(outcome));
    }

    /**
     * PUT /api/intervention-outcomes/{id}/complete
     * Complete an intervention with post-assessment scores
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionOutcomeDTO> completeIntervention(
            @PathVariable Long id,
            @Valid @RequestBody InterventionOutcomeRequest request) {

        InterventionOutcome outcome = outcomeService.completeIntervention(
                id,
                request.getPostStressScore(),
                request.getPostDepressionScore(),
                request.getPostAnxietyScore()
        );

        return ResponseEntity.ok(InterventionOutcomeDTO.fromEntity(outcome));
    }

    /**
     * PUT /api/intervention-outcomes/{id}/dropout
     * Record dropout from intervention
     */
    @PutMapping("/{id}/dropout")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionOutcomeDTO> recordDropout(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Integer dropoutWeek) {

        InterventionOutcome outcome = outcomeService.recordDropout(id, reason, dropoutWeek);
        return ResponseEntity.ok(InterventionOutcomeDTO.fromEntity(outcome));
    }

    /**
     * PUT /api/intervention-outcomes/{id}/adherence
     * Update adherence metrics
     */
    @PutMapping("/{id}/adherence")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionOutcomeDTO> updateAdherence(
            @PathVariable Long id,
            @RequestParam Integer sessionsCompleted,
            @RequestParam(required = false) Integer sessionsScheduled) {

        InterventionOutcome outcome = outcomeService.updateAdherence(id, sessionsCompleted, sessionsScheduled);
        return ResponseEntity.ok(InterventionOutcomeDTO.fromEntity(outcome));
    }

    /**
     * PUT /api/intervention-outcomes/{id}/feedback
     * Add user feedback
     */
    @PutMapping("/{id}/feedback")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionOutcomeDTO> addFeedback(
            @PathVariable Long id,
            @Valid @RequestBody InterventionOutcomeRequest request) {

        InterventionOutcome outcome = outcomeService.addUserFeedback(
                id,
                request.getUserSatisfactionScore(),
                request.getUserFeedback(),
                request.getWouldRecommend()
        );

        return ResponseEntity.ok(InterventionOutcomeDTO.fromEntity(outcome));
    }

    // ==================== USER QUERIES ====================

    /**
     * GET /api/intervention-outcomes/me
     * Get current user's intervention history
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getMyOutcomes(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        List<InterventionOutcome> outcomes = outcomeService.getUserInterventionHistory(userId);

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/intervention-outcomes/me/active
     * Get current user's active interventions
     */
    @GetMapping("/me/active")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getMyActiveInterventions(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        List<InterventionOutcome> outcomes = outcomeService.getUserActiveInterventions(userId);

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/intervention-outcomes/me/best
     * Get current user's best outcomes
     */
    @GetMapping("/me/best")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getMyBestOutcomes(
            @RequestParam(required = false, defaultValue = "5") Integer limit,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        List<InterventionOutcome> outcomes = outcomeService.getUserBestOutcomes(userId, limit);

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/intervention-outcomes/user/{userId}
     * Get specific user's outcomes (Healthcare Professional/Admin only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getUserOutcomes(@PathVariable Long userId) {
        List<InterventionOutcome> outcomes = outcomeService.getUserInterventionHistory(userId);

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==================== SIMULATION ENDPOINTS (Component 1) ====================

    /**
     * POST /api/intervention-outcomes/simulate
     * Create a simulated intervention outcome
     */
    @PostMapping("/simulate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionOutcomeDTO> createSimulatedOutcome(
            @Valid @RequestBody SimulationRequest request) {

        InterventionOutcome outcome = outcomeService.createSimulatedOutcome(
                findInterventionById(request.getInterventionId()),
                request.getPreStressScore(),
                request.getPreDepressionScore(),
                request.getPreAnxietyScore(),
                request.getPostStressScore(),
                request.getPostDepressionScore(),
                request.getPostAnxietyScore(),
                request.getSimulationModelVersion()
        );

        if (request.getConfidenceScore() != null) {
            outcome.setConfidenceScore(request.getConfidenceScore());
        }
        if (request.getNoiseLevel() != null) {
            outcome.setNoiseLevel(request.getNoiseLevel());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InterventionOutcomeDTO.fromEntity(outcome));
    }

    /**
     * POST /api/intervention-outcomes/simulate/bulk
     * Create multiple simulated outcomes
     */
    @PostMapping("/simulate/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InterventionOutcomeDTO>> createBulkSimulatedOutcomes(
            @Valid @RequestBody SimulationRequest.BulkSimulationRequest request) {

        List<InterventionOutcomeService.SimulatedOutcomeRequest> serviceRequests = request.getSimulations().stream()
                .map(sim -> new InterventionOutcomeService.SimulatedOutcomeRequest(
                        sim.getInterventionId(),
                        sim.getPreStressScore(),
                        sim.getPreDepressionScore(),
                        sim.getPreAnxietyScore(),
                        sim.getPostStressScore(),
                        sim.getPostDepressionScore(),
                        sim.getPostAnxietyScore(),
                        sim.getSimulationModelVersion(),
                        sim.getConfidenceScore(),
                        sim.getNoiseLevel()
                ))
                .collect(Collectors.toList());

        List<InterventionOutcome> outcomes = outcomeService.createBulkSimulatedOutcomes(serviceRequests);

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    /**
     * GET /api/intervention-outcomes/simulated
     * Get all simulated outcomes
     */
    @GetMapping("/simulated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getSimulatedOutcomes(
            @RequestParam(required = false) String modelVersion) {

        List<InterventionOutcome> outcomes = modelVersion != null
                ? outcomeService.getSimulatedOutcomesByVersion(modelVersion)
                : outcomeService.getSimulatedOutcomes();

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/intervention-outcomes/simulation/statistics
     * Get simulation statistics
     */
    @GetMapping("/simulation/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getSimulationStatistics() {
        return ResponseEntity.ok(outcomeService.getSimulationStatistics());
    }

    // ==================== ANALYSIS ENDPOINTS ====================

    /**
     * GET /api/intervention-outcomes/intervention/{interventionId}/statistics
     * Get statistics for a specific intervention
     */
    @GetMapping("/intervention/{interventionId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getInterventionStatistics(
            @PathVariable Long interventionId) {
        return ResponseEntity.ok(outcomeService.getInterventionStatistics(interventionId));
    }

    /**
     * GET /api/intervention-outcomes/statistics
     * Get overall system statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getOverallStatistics() {
        return ResponseEntity.ok(outcomeService.getOverallStatistics());
    }

    /**
     * GET /api/intervention-outcomes/effectiveness/{rating}
     * Get outcomes by effectiveness rating
     */
    @GetMapping("/effectiveness/{rating}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getByEffectiveness(
            @PathVariable EffectivenessRating rating) {

        List<InterventionOutcome> outcomes = outcomeService.findByEffectivenessRating(rating);

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/intervention-outcomes/effectiveness-by-type
     * Get effectiveness breakdown by intervention type
     */
    @GetMapping("/effectiveness-by-type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<Object[]>> getEffectivenessByType() {
        return ResponseEntity.ok(outcomeService.getEffectivenessByInterventionType());
    }

    // ==================== CLUSTER ANALYSIS ====================

    /**
     * GET /api/intervention-outcomes/cluster-transitions
     * Get outcomes with cluster transitions
     */
    @GetMapping("/cluster-transitions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getClusterTransitions() {
        List<InterventionOutcome> outcomes = outcomeService.getOutcomesWithClusterTransitions();

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/intervention-outcomes/cluster-transition-matrix
     * Get cluster transition matrix
     */
    @GetMapping("/cluster-transition-matrix")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<Object[]>> getClusterTransitionMatrix() {
        return ResponseEntity.ok(outcomeService.getClusterTransitionMatrix());
    }

    // ==================== DATE RANGE QUERIES ====================

    /**
     * GET /api/intervention-outcomes/date-range
     * Get outcomes within date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<InterventionOutcome> outcomes = outcomeService.findByDateRange(start, end);

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==================== REVIEW WORKFLOW ====================

    /**
     * GET /api/intervention-outcomes/pending-review
     * Get outcomes pending review
     */
    @GetMapping("/pending-review")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<InterventionOutcomeDTO>> getPendingReview() {
        List<InterventionOutcome> outcomes = outcomeService.getPendingReviewOutcomes();

        List<InterventionOutcomeDTO> dtos = outcomes.stream()
                .map(InterventionOutcomeDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * PUT /api/intervention-outcomes/{id}/review
     * Mark outcome as reviewed
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<InterventionOutcomeDTO> markAsReviewed(
            @PathVariable Long id,
            @RequestParam String reviewedBy,
            @RequestParam(required = false) String notes) {

        InterventionOutcome outcome = outcomeService.markAsReviewed(id, reviewedBy, notes);
        return ResponseEntity.ok(InterventionOutcomeDTO.fromEntity(outcome));
    }

    // ==================== PAGINATION ====================

    /**
     * GET /api/intervention-outcomes/paginated
     * Get paginated outcomes by status
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Page<InterventionOutcomeDTO>> getPaginatedOutcomes(
            @RequestParam(required = false) OutcomeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "interventionStartDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InterventionOutcome> outcomePage = status != null
                ? outcomeService.findByStatusPaginated(status, pageable)
                : outcomeService.findSimulatedPaginated(pageable);

        Page<InterventionOutcomeDTO> dtoPage = outcomePage.map(InterventionOutcomeDTO::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }

    // ==================== HELPER METHODS ====================

    private Long getCurrentUserId(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal =
                (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

    private com.research.mano.entity.Intervention findInterventionById(Long id) {
        // This would typically be injected, but for simplicity:
        return null; // The service will handle the lookup
    }
}