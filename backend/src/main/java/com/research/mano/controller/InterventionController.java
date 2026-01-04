package com.research.mano.controller;

import com.research.mano.controller.request.InterventionRequest;
import com.research.mano.controller.responce.InterventionDTO;
import com.research.mano.entity.Intervention;
import com.research.mano.entity.Intervention.InterventionType;
import com.research.mano.entity.Intervention.IntensityLevel;
import com.research.mano.entity.Intervention.EvidenceLevel;
import com.research.mano.entity.Intervention.ValidationStatus;
import com.research.mano.service.InterventionService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Intervention Controller for Component 1 (AMISE)
 * REST API endpoints for intervention management and recommendations
 */
@RestController
@RequestMapping("/interventions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InterventionController {

    @Autowired
    private InterventionService interventionService;

    // ==================== CRUD OPERATIONS ====================

    /**
     * GET /api/interventions
     * Get all active interventions
     */
    @GetMapping
    public ResponseEntity<List<InterventionDTO>> getAllInterventions(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {

        List<Intervention> interventions = activeOnly
                ? interventionService.findAllActive()
                : interventionService.findAll();

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/{id}
     * Get intervention by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<InterventionDTO> getInterventionById(@PathVariable Long id) {
        Intervention intervention = interventionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention not found with id: " + id));

        return ResponseEntity.ok(InterventionDTO.fromEntity(intervention));
    }

    /**
     * GET /api/interventions/code/{code}
     * Get intervention by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<InterventionDTO> getInterventionByCode(@PathVariable String code) {
        Intervention intervention = interventionService.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Intervention not found with code: " + code));

        return ResponseEntity.ok(InterventionDTO.fromEntity(intervention));
    }

    /**
     * POST /api/interventions
     * Create new intervention (Admin/Healthcare Professional only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionDTO> createIntervention(
            @Valid @RequestBody InterventionRequest request) {

        Intervention intervention = mapRequestToEntity(request);
        Intervention created = interventionService.createIntervention(intervention);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InterventionDTO.fromEntity(created));
    }

    /**
     * PUT /api/interventions/{id}
     * Update intervention (Admin/Healthcare Professional only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<InterventionDTO> updateIntervention(
            @PathVariable Long id,
            @Valid @RequestBody InterventionRequest request) {

        Intervention intervention = mapRequestToEntity(request);
        Intervention updated = interventionService.updateIntervention(id, intervention);

        return ResponseEntity.ok(InterventionDTO.fromEntity(updated));
    }

    /**
     * DELETE /api/interventions/{id}
     * Deactivate intervention (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InterventionDTO> deactivateIntervention(@PathVariable Long id) {
        Intervention deactivated = interventionService.deactivateIntervention(id);
        return ResponseEntity.ok(InterventionDTO.fromEntity(deactivated));
    }

    /**
     * PUT /api/interventions/{id}/activate
     * Activate intervention (Admin only)
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InterventionDTO> activateIntervention(@PathVariable Long id) {
        Intervention activated = interventionService.activateIntervention(id);
        return ResponseEntity.ok(InterventionDTO.fromEntity(activated));
    }

    // ==================== FILTERING ====================

    /**
     * GET /api/interventions/type/{type}
     * Get interventions by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<InterventionDTO>> getByType(@PathVariable InterventionType type) {
        List<Intervention> interventions = interventionService.findActiveByType(type);

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/intensity/{intensity}
     * Get interventions by intensity
     */
    @GetMapping("/intensity/{intensity}")
    public ResponseEntity<List<InterventionDTO>> getByIntensity(@PathVariable IntensityLevel intensity) {
        List<Intervention> interventions = interventionService.findByIntensity(intensity);

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/evidence/{level}
     * Get interventions by evidence level
     */
    @GetMapping("/evidence/{level}")
    public ResponseEntity<List<InterventionDTO>> getByEvidenceLevel(@PathVariable EvidenceLevel level) {
        List<Intervention> interventions = interventionService.findByEvidenceLevel(level);

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/validated
     * Get validated interventions only
     */
    @GetMapping("/validated")
    public ResponseEntity<List<InterventionDTO>> getValidatedInterventions() {
        List<Intervention> interventions = interventionService.findValidatedInterventions();

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/evidence-based
     * Get evidence-based interventions (strong/moderate evidence)
     */
    @GetMapping("/evidence-based")
    public ResponseEntity<List<InterventionDTO>> getEvidenceBasedInterventions() {
        List<Intervention> interventions = interventionService.findEvidenceBasedInterventions();

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==================== RECOMMENDATIONS ====================

    /**
     * GET /api/interventions/recommendations
     * Get personalized intervention recommendations based on mental health scores
     */
    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionDTO>> getRecommendations(
            @RequestParam Double stressScore,
            @RequestParam Double depressionScore,
            @RequestParam Double anxietyScore,
            @RequestParam(required = false, defaultValue = "10") Integer maxResults) {

        List<Intervention> recommendations = interventionService.getRecommendationsForScores(
                stressScore, depressionScore, anxietyScore, maxResults);

        List<InterventionDTO> dtos = recommendations.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/recommendations/constrained
     * Get recommendations with constraints
     */
    @GetMapping("/recommendations/constrained")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionDTO>> getConstrainedRecommendations(
            @RequestParam(required = false) InterventionType preferredType,
            @RequestParam(required = false) IntensityLevel maxIntensity,
            @RequestParam(required = false) Integer maxDurationWeeks,
            @RequestParam(required = false, defaultValue = "10") Integer maxResults) {

        List<Intervention> recommendations = interventionService.getRecommendationsWithConstraints(
                preferredType, maxIntensity, maxDurationWeeks, maxResults);

        List<InterventionDTO> dtos = recommendations.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/crisis
     * Get crisis interventions
     */
    @GetMapping("/crisis")
    public ResponseEntity<List<InterventionDTO>> getCrisisInterventions() {
        List<Intervention> interventions = interventionService.getCrisisInterventions();

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==================== BINAURAL BEATS (Component 1 Feature) ====================

    /**
     * GET /api/interventions/binaural
     * Get all binaural beats interventions
     */
    @GetMapping("/binaural")
    public ResponseEntity<List<InterventionDTO>> getBinauralInterventions() {
        List<Intervention> interventions = interventionService.findBinauralInterventions();

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/binaural/recommendation
     * Get binaural beats recommendation for target mental state
     */
    @GetMapping("/binaural/recommendation")
    public ResponseEntity<List<InterventionDTO>> getBinauralRecommendation(
            @RequestParam String targetState) {

        List<Intervention> interventions = interventionService.getBinauralRecommendation(targetState);

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/interventions/binaural/frequency-range
     * Get binaural interventions by frequency range
     */
    @GetMapping("/binaural/frequency-range")
    public ResponseEntity<List<InterventionDTO>> getBinauralByFrequencyRange(
            @RequestParam Double minHz,
            @RequestParam Double maxHz) {

        List<Intervention> interventions = interventionService.findByBinauralFrequencyRange(minHz, maxHz);

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==================== SIMULATION SUPPORT ====================

    /**
     * GET /api/interventions/simulatable
     * Get interventions that can be simulated
     */
    @GetMapping("/simulatable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<InterventionDTO>> getSimulatableInterventions() {
        List<Intervention> interventions = interventionService.findSimulatableInterventions();

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==================== SEARCH ====================

    /**
     * GET /api/interventions/search
     * Search interventions by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<InterventionDTO>> searchInterventions(
            @RequestParam String keyword) {

        List<Intervention> interventions = interventionService.searchByKeyword(keyword);

        List<InterventionDTO> dtos = interventions.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==================== STATISTICS ====================

    /**
     * GET /api/interventions/statistics
     * Get intervention statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = Map.of(
                "countByType", interventionService.countByType(),
                "countByIntensity", interventionService.countByIntensity(),
                "countByEvidenceLevel", interventionService.countByEvidenceLevel(),
                "averageExpectedReductions", interventionService.getAverageExpectedReductions(),
                "totalCount", interventionService.count()
        );

        return ResponseEntity.ok(stats);
    }

    // ==================== VALIDATION WORKFLOW ====================

    /**
     * PUT /api/interventions/{id}/validation
     * Update validation status (Admin only)
     */
    @PutMapping("/{id}/validation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InterventionDTO> updateValidationStatus(
            @PathVariable Long id,
            @RequestParam ValidationStatus status,
            @RequestParam String reviewedBy) {

        Intervention updated = interventionService.updateValidationStatus(id, status, reviewedBy);
        return ResponseEntity.ok(InterventionDTO.fromEntity(updated));
    }

    // ==================== BULK OPERATIONS ====================

    /**
     * POST /api/interventions/bulk
     * Create multiple interventions (Admin only)
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InterventionDTO>> createBulkInterventions(
            @Valid @RequestBody List<InterventionRequest> requests) {

        List<Intervention> interventions = requests.stream()
                .map(this::mapRequestToEntity)
                .collect(Collectors.toList());

        List<Intervention> created = interventionService.createBulkInterventions(interventions);

        List<InterventionDTO> dtos = created.stream()
                .map(InterventionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    // ==================== HELPER METHODS ====================

    private Intervention mapRequestToEntity(InterventionRequest request) {
        Intervention intervention = new Intervention();

        intervention.setInterventionCode(request.getInterventionCode());
        intervention.setName(request.getName());
        intervention.setDescription(request.getDescription());
        intervention.setInterventionType(request.getInterventionType());
        intervention.setIntensityLevel(request.getIntensityLevel());
        intervention.setDurationWeeks(request.getDurationWeeks());
        intervention.setFrequencyPerWeek(request.getFrequencyPerWeek());
        intervention.setSessionDurationMinutes(request.getSessionDurationMinutes());

        intervention.setExpectedStressReduction(request.getExpectedStressReduction());
        intervention.setExpectedDepressionReduction(request.getExpectedDepressionReduction());
        intervention.setExpectedAnxietyReduction(request.getExpectedAnxietyReduction());
        intervention.setExpectedResilienceIncrease(request.getExpectedResilienceIncrease());

        intervention.setEffectConfidenceLower(request.getEffectConfidenceLower());
        intervention.setEffectConfidenceUpper(request.getEffectConfidenceUpper());

        intervention.setEvidenceLevel(request.getEvidenceLevel());
        intervention.setResearchReferences(request.getResearchReferences());
        intervention.setContraindications(request.getContraindications());
        intervention.setPrerequisites(request.getPrerequisites());
        intervention.setRecommendedFor(request.getRecommendedFor());

        intervention.setSimulationModelId(request.getSimulationModelId());
        intervention.setSimulationParameters(request.getSimulationParameters());

        intervention.setBinauralFrequencyHz(request.getBinauralFrequencyHz());
        intervention.setBinauralSessionMinutes(request.getBinauralSessionMinutes());
        intervention.setComplementaryResources(request.getComplementaryResources());

        intervention.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        intervention.setCreatedBy(request.getCreatedBy());

        return intervention;
    }
}