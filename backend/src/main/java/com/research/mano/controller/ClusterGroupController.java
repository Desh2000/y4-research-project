package com.research.mano.controller;

import com.research.mano.controller.responce.ClusterGroupDTO;
import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.exception.ClusterNotFoundException;
import com.research.mano.exception.InvalidScoreRangeException;
import com.research.mano.exception.ValidationException;
import com.research.mano.service.ClusterGroupService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cluster Group Controller
 * Handles Component 4 (GMM Clustering System) REST API endpoints
 */
@RestController
@RequestMapping("/api/mental-health/clusters")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClusterGroupController {

    private final ClusterGroupService clusterGroupService;

    public ClusterGroupController(ClusterGroupService clusterGroupService) {
        this.clusterGroupService = clusterGroupService;
    }

    /**
     * GET /api/mental-health/clusters
     * Get all cluster groups
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ClusterGroupDTO>> getAllClusters() {
        List<ClusterGroup> clusters = clusterGroupService.findAll();

        List<ClusterGroupDTO> clusterDTOs = clusters.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clusterDTOs);
    }

    /**
     * GET /api/mental-health/clusters/{id}
     * Get cluster by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ClusterGroupDTO> getClusterById(@PathVariable Long id) {
        ClusterGroup cluster = clusterGroupService.findById(id)
                .orElseThrow(() -> new ClusterNotFoundException(id));

        return ResponseEntity.ok(convertToDTO(cluster));
    }

    /**
     * GET /api/mental-health/clusters/identifier/{clusterIdentifier}
     * Get cluster by identifier
     */
    @GetMapping("/identifier/{clusterIdentifier}")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ClusterGroupDTO> getClusterByIdentifier(@PathVariable String clusterIdentifier) {
        ClusterGroup cluster = clusterGroupService.findByClusterIdentifier(clusterIdentifier)
                .orElseThrow(() -> new ClusterNotFoundException(clusterIdentifier));

        return ResponseEntity.ok(convertToDTO(cluster));
    }

    /**
     * GET /api/mental-health/clusters/category/{category}
     * Get clusters by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ClusterGroupDTO>> getClustersByCategory(@PathVariable String category) {
        try {
            MentalHealthPrediction.ClusterCategory clusterCategory =
                    MentalHealthPrediction.ClusterCategory.valueOf(category.toUpperCase());

            List<ClusterGroup> clusters = clusterGroupService.getClustersByCategory(clusterCategory);

            List<ClusterGroupDTO> clusterDTOs = clusters.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(clusterDTOs);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("category", "Invalid cluster category: " + category);
        }
    }

    /**
     * GET /api/mental-health/clusters/level/{level}
     * Get clusters by level
     */
    @GetMapping("/level/{level}")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ClusterGroupDTO>> getClustersByLevel(@PathVariable String level) {
        try {
            MentalHealthPrediction.ClusterLevel clusterLevel =
                    MentalHealthPrediction.ClusterLevel.valueOf(level.toUpperCase());

            List<ClusterGroup> clusters = clusterGroupService.getClustersByLevel(clusterLevel);

            List<ClusterGroupDTO> clusterDTOs = clusters.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(clusterDTOs);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("level", "Invalid cluster level: " + level);
        }
    }

    /**
     * GET /api/mental-health/clusters/find-for-scores
     * Find appropriate cluster for given scores
     */
    @GetMapping("/find-for-scores")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ClusterGroupDTO> findClusterForScores(
            @RequestParam Double stressScore,
            @RequestParam Double depressionScore,
            @RequestParam Double anxietyScore) {

        // Validate scores
        if (stressScore < 0.0 || stressScore > 1.0 ||
                depressionScore < 0.0 || depressionScore > 1.0 ||
                anxietyScore < 0.0 || anxietyScore > 1.0) {
            throw new InvalidScoreRangeException();
        }

        ClusterGroup cluster = clusterGroupService.findClusterForPrediction(stressScore, depressionScore, anxietyScore)
                .orElseThrow(() -> new ClusterNotFoundException("No suitable cluster found for given scores"));

        return ResponseEntity.ok(convertToDTO(cluster));
    }

    /**
     * GET /api/mental-health/clusters/most-populated
     * Get most populated clusters
     */
    @GetMapping("/most-populated")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ClusterGroupDTO>> getMostPopulatedClusters() {
        List<ClusterGroup> clusters = clusterGroupService.getMostPopulatedClusters();

        List<ClusterGroupDTO> clusterDTOs = clusters.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clusterDTOs);
    }

    /**
     * GET /api/mental-health/clusters/highest-resilience
     * Get clusters with highest resilience
     */
    @GetMapping("/highest-resilience")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ClusterGroupDTO>> getHighestResilienceClusters() {
        List<ClusterGroup> clusters = clusterGroupService.getHighestResilienceClusters();

        List<ClusterGroupDTO> clusterDTOs = clusters.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clusterDTOs);
    }

    /**
     * GET /api/mental-health/clusters/lowest-resilience
     * Get clusters with lowest resilience
     */
    @GetMapping("/lowest-resilience")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ClusterGroupDTO>> getLowestResilienceClusters() {
        List<ClusterGroup> clusters = clusterGroupService.getLowestResilienceClusters();

        List<ClusterGroupDTO> clusterDTOs = clusters.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clusterDTOs);
    }

    /**
     * GET /api/mental-health/clusters/support-level/{supportLevel}
     * Get clusters by professional support level
     */
    @GetMapping("/support-level/{supportLevel}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ClusterGroupDTO>> getClustersBySupportLevel(@PathVariable String supportLevel) {
        try {
            ClusterGroup.ProfessionalSupportLevel level =
                    ClusterGroup.ProfessionalSupportLevel.valueOf(supportLevel.toUpperCase());

            List<ClusterGroup> clusters = clusterGroupService.findByProfessionalSupportLevel(level);

            List<ClusterGroupDTO> clusterDTOs = clusters.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(clusterDTOs);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("supportLevel", "Invalid support level: " + supportLevel);
        }
    }

    /**
     * GET /api/mental-health/clusters/statistics/distribution
     * Get cluster distribution statistics
     */
    @GetMapping("/statistics/distribution")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getClusterDistributionStats() {
        List<Object[]> stats = clusterGroupService.getClusterDistributionStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/mental-health/clusters/statistics/by-category
     * Get statistics by category
     */
    @GetMapping("/statistics/by-category")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getStatsByCategory() {
        List<Object[]> stats = clusterGroupService.getStatsByCategory();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/mental-health/clusters/statistics/by-level
     * Get statistics by level
     */
    @GetMapping("/statistics/by-level")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getStatsByLevel() {
        List<Object[]> stats = clusterGroupService.getStatsByLevel();
        return ResponseEntity.ok(stats);
    }

    /**
     * POST /api/mental-health/clusters/initialize
     * Initialize all required clusters (Admin only)
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> initializeAllClusters() {
        clusterGroupService.initializeAllClusters();
        return ResponseEntity.ok(new ApiResponse(true, "All clusters initialized successfully"));
    }

    /**
     * PUT /api/mental-health/clusters/{id}/centroid
     * Update cluster centroid (Admin only)
     */
    @PutMapping("/{id}/centroid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClusterGroupDTO> updateClusterCentroid(
            @PathVariable Long id,
            @RequestParam Double stressCentroid,
            @RequestParam Double depressionCentroid,
            @RequestParam Double anxietyCentroid,
            @RequestParam String modelVersion) {

        ClusterGroup updatedCluster = clusterGroupService.updateClusterCentroid(
                id, stressCentroid, depressionCentroid, anxietyCentroid, modelVersion
        );

        return ResponseEntity.ok(convertToDTO(updatedCluster));
    }

    /**
     * PUT /api/mental-health/clusters/{id}/recommendations
     * Update cluster recommendations (Healthcare Professional/Admin only)
     */
    @PutMapping("/{id}/recommendations")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ClusterGroupDTO> updateClusterRecommendations(
            @PathVariable Long id,
            @RequestParam String interventions,
            @RequestParam String peerActivities) {

        ClusterGroup updatedCluster = clusterGroupService.updateClusterRecommendations(
                id, interventions, peerActivities
        );

        return ResponseEntity.ok(convertToDTO(updatedCluster));
    }

    /**
     * POST /api/mental-health/clusters/{id}/recalculate-statistics
     * Recalculate cluster statistics (Admin only)
     */
    @PostMapping("/{id}/recalculate-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> recalculateClusterStatistics(@PathVariable Long id) {
        clusterGroupService.recalculateClusterStatistics(id);
        return ResponseEntity.ok(new ApiResponse(true, "Cluster statistics recalculated"));
    }

    /**
     * GET /api/mental-health/clusters/validate-integrity
     * Validate cluster system integrity (Admin only)
     */
    @GetMapping("/validate-integrity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validateClusterIntegrity() {
        boolean isValid = clusterGroupService.validateClusterIntegrity();

        if (isValid) {
            return ResponseEntity.ok(new ApiResponse(true, "Cluster system integrity is valid"));
        } else {
            List<String> missing = clusterGroupService.getMissingClusterIdentifiers();
            return ResponseEntity.ok(new IntegrityValidationResponse(false, "Cluster system integrity issues found", missing));
        }
    }

    /**
     * GET /api/mental-health/clusters/total-members
     * Get total member count across all clusters
     */
    @GetMapping("/total-members")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<Long> getTotalMemberCount() {
        Long totalMembers = clusterGroupService.getTotalMemberCount();
        return ResponseEntity.ok(totalMembers);
    }

    // Helper methods
    private ClusterGroupDTO convertToDTO(ClusterGroup cluster) {
        ClusterGroupDTO dto = new ClusterGroupDTO();
        dto.setId(cluster.getId());
        dto.setClusterIdentifier(cluster.getClusterIdentifier());
        if (cluster.getCategory() != null) {
            dto.setCategory(cluster.getCategory().name());
        }
        if (cluster.getLevel() != null) {
            dto.setLevel(cluster.getLevel().name());
        }
        dto.setDescription(cluster.getClusterDescription());
        dto.setMemberCount(Math.toIntExact(cluster.getMemberCount()));
        dto.setAverageResilienceScore(cluster.getAverageResilienceScore());
        if (cluster.getProfessionalSupportLevel() != null) {
            dto.setProfessionalSupportLevel(cluster.getProfessionalSupportLevel().name());
        }
        dto.setRecommendedInterventions(cluster.getRecommendedInterventions());
        dto.setPeerSupportActivities(cluster.getPeerSupportActivities());
        dto.setLastUpdated(cluster.getLastUpdated());
        dto.setModelVersion(cluster.getModelVersion());

        // GMM parameters
        dto.setCentroidStress(cluster.getCentroidStress());
        dto.setCentroidDepression(cluster.getCentroidDepression());
        dto.setCentroidAnxiety(cluster.getCentroidAnxiety());
        dto.setCovarianceMatrix(cluster.getCovarianceMatrix());
        dto.setClusterWeight(cluster.getClusterWeight());

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

    /**
     * Response for integrity validation
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class IntegrityValidationResponse {
        private boolean valid;
        private String message;
        private List<String> missingClusters;
    }
}
