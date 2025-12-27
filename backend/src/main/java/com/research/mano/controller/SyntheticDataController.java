package com.research.mano.controller;

import com.research.mano.controller.request.*;
import com.research.mano.controller.responce.*;
import com.research.mano.entity.SyntheticDataRecord;
import com.research.mano.service.SyntheticDataRecordService;
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
 * Synthetic Data Controller
 * Handles Component 1 (Privacy-Preserving Data Generation System) REST API endpoints
 */
@RestController
@RequestMapping("/api/synthetic-data")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SyntheticDataController {

    @Autowired
    private SyntheticDataRecordService syntheticDataService;

    /**
     * POST /api/synthetic-data/generate
     * Generate synthetic data record (Researcher/Admin only)
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<SyntheticDataRecordDTO> generateSyntheticData(
            @Valid @RequestBody SyntheticDataGenerationRequest generationRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);

        try {
            SyntheticDataRecord.GenerationMethod method =
                    SyntheticDataRecord.GenerationMethod.valueOf(generationRequest.getGenerationMethod().toUpperCase());

            SyntheticDataRecord record = syntheticDataService.generateSyntheticRecord(
                    generationRequest.getSourceUserCluster(),
                    method,
                    generationRequest.getResearcherId() != null ?
                            generationRequest.getResearcherId() : userPrincipal.getUsername(),
                    generationRequest.getResearchPurpose()
            );

            return ResponseEntity.ok(convertToDTO(record));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("generationMethod", "Invalid generation method: " + generationRequest.getGenerationMethod());
        }
    }

    /**
     * POST /api/synthetic-data/generate-batch
     * Generate batch of synthetic data records (Researcher/Admin only)
     */
    @PostMapping("/generate-batch")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> generateBatchSyntheticData(
            @Valid @RequestBody SyntheticDataGenerationRequest generationRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);

        try {
            SyntheticDataRecord.GenerationMethod method =
                    SyntheticDataRecord.GenerationMethod.valueOf(generationRequest.getGenerationMethod().toUpperCase());

            List<SyntheticDataRecord> records = syntheticDataService.generateBatchSyntheticRecords(
                    generationRequest.getSourceUserCluster(),
                    method,
                    generationRequest.getBatchSize(),
                    generationRequest.getResearcherId() != null ?
                            generationRequest.getResearcherId() : userPrincipal.getUsername(),
                    generationRequest.getResearchPurpose()
            );

            List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(recordDTOs);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("generationMethod", "Invalid generation method: " + generationRequest.getGenerationMethod());
        }
    }

    /**
     * GET /api/synthetic-data
     * Get all synthetic data records with pagination (Researcher/Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<Page<SyntheticDataRecordDTO>> getAllSyntheticData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "generationTimestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String generationMethod,
            @RequestParam(required = false) String sourceCluster,
            @RequestParam(required = false) Boolean isValidated) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        List<SyntheticDataRecord> records;

        if (generationMethod != null) {
            try {
                SyntheticDataRecord.GenerationMethod method =
                        SyntheticDataRecord.GenerationMethod.valueOf(generationMethod.toUpperCase());
                records = syntheticDataService.findByGenerationMethod(method);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("generationMethod", "Invalid generation method: " + generationMethod);
            }
        } else if (sourceCluster != null) {
            records = syntheticDataService.findBySourceUserCluster(sourceCluster);
        } else if (Boolean.TRUE.equals(isValidated)) {
            records = syntheticDataService.findValidatedRecords();
        } else if (Boolean.FALSE.equals(isValidated)) {
            records = syntheticDataService.findRecordsNeedingValidation();
        } else {
            records = syntheticDataService.findAll();
        }

        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, recordDTOs.size());
        List<SyntheticDataRecordDTO> pageContent = start < recordDTOs.size() ?
                recordDTOs.subList(start, end) : List.of();

        Page<SyntheticDataRecordDTO> pageResult = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, recordDTOs.size()
        );

        return ResponseEntity.ok(pageResult);
    }

    /**
     * GET /api/synthetic-data/{recordId}
     * Get synthetic data record by record ID (Researcher/Admin only)
     */
    @GetMapping("/{recordId}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<SyntheticDataRecordDTO> getSyntheticDataByRecordId(@PathVariable String recordId) {
        SyntheticDataRecord record = syntheticDataService.findByRecordId(recordId)
                .orElseThrow(() -> new SyntheticDataRecordNotFoundException(recordId));

        return ResponseEntity.ok(convertToDTO(record));
    }

    /**
     * GET /api/synthetic-data/by-researcher/{researcherId}
     * Get synthetic data records by researcher (Researcher/Admin only)
     */
    @GetMapping("/by-researcher/{researcherId}")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getSyntheticDataByResearcher(@PathVariable String researcherId) {
        List<SyntheticDataRecord> records = syntheticDataService.findByResearcher(researcherId);
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/by-research-purpose
     * Get synthetic data records by research purpose (Researcher/Admin only)
     */
    @GetMapping("/by-research-purpose")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getSyntheticDataByResearchPurpose(
            @RequestParam String purpose) {

        List<SyntheticDataRecord> records = syntheticDataService.findByResearchPurpose(purpose);
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/high-quality
     * Get high-quality synthetic data records (Researcher/Admin only)
     */
    @GetMapping("/high-quality")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getHighQualitySyntheticData() {
        List<SyntheticDataRecord> records = syntheticDataService.findHighQualityRecords();
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/validated
     * Get validated synthetic data records (Researcher/Admin only)
     */
    @GetMapping("/validated")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getValidatedSyntheticData() {
        List<SyntheticDataRecord> records = syntheticDataService.findValidatedRecords();
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/privacy-score-range
     * Get synthetic data records by privacy score range (Researcher/Admin only)
     */
    @GetMapping("/privacy-score-range")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getSyntheticDataByPrivacyScore(
            @RequestParam Double minScore,
            @RequestParam Double maxScore) {

        if (minScore < 0.0 || minScore > 1.0 || maxScore < 0.0 || maxScore > 1.0 || minScore > maxScore) {
            throw new ValidationException("Privacy scores must be between 0.0 and 1.0, and minScore <= maxScore");
        }

        List<SyntheticDataRecord> records = syntheticDataService.findByPrivacyScoreRange(minScore, maxScore);
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/utility-score-range
     * Get synthetic data records by utility score range (Researcher/Admin only)
     */
    @GetMapping("/utility-score-range")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getSyntheticDataByUtilityScore(
            @RequestParam Double minScore,
            @RequestParam Double maxScore) {

        if (minScore < 0.0 || minScore > 1.0 || maxScore < 0.0 || maxScore > 1.0 || minScore > maxScore) {
            throw new ValidationException("Utility scores must be between 0.0 and 1.0, and minScore <= maxScore");
        }

        List<SyntheticDataRecord> records = syntheticDataService.findByUtilityScoreRange(minScore, maxScore);
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/date-range
     * Get synthetic data records by generation date range (Researcher/Admin only)
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getSyntheticDataByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<SyntheticDataRecord> records = syntheticDataService.findByGenerationDateRange(startDate, endDate);
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/best-privacy-utility-balance
     * Get records with best privacy-utility balance (Researcher/Admin only)
     */
    @GetMapping("/best-privacy-utility-balance")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> getBestPrivacyUtilityBalance(
            @RequestParam(defaultValue = "0.7") Double threshold) {

        List<SyntheticDataRecord> records = syntheticDataService.findBestPrivacyUtilityBalance(threshold);
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * PUT /api/synthetic-data/{id}/validate
     * Validate synthetic data record (Researcher/Admin only)
     */
    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<SyntheticDataRecordDTO> validateSyntheticData(
            @PathVariable Long id,
            @RequestParam Double validationScore) {

        if (validationScore < 0.0 || validationScore > 1.0) {
            throw new ValidationException("Validation score must be between 0.0 and 1.0");
        }

        SyntheticDataRecord validatedRecord = syntheticDataService.validateRecord(id, validationScore);
        return ResponseEntity.ok(convertToDTO(validatedRecord));
    }

    /**
     * PUT /api/synthetic-data/{id}/update-quality-scores
     * Update privacy and utility scores (Researcher/Admin only)
     */
    @PutMapping("/{id}/update-quality-scores")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<SyntheticDataRecordDTO> updateQualityScores(
            @PathVariable Long id,
            @RequestParam Double privacyScore,
            @RequestParam Double utilityScore) {

        if (privacyScore < 0.0 || privacyScore > 1.0 || utilityScore < 0.0 || utilityScore > 1.0) {
            throw new ValidationException("Privacy and utility scores must be between 0.0 and 1.0");
        }

        SyntheticDataRecord updatedRecord = syntheticDataService.updateQualityScores(id, privacyScore, utilityScore);
        return ResponseEntity.ok(convertToDTO(updatedRecord));
    }

    /**
     * POST /api/synthetic-data/{id}/anonymize
     * Process additional anonymization for record (Admin only)
     */
    @PostMapping("/{id}/anonymize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SyntheticDataRecordDTO> processAnonymization(@PathVariable Long id) {
        SyntheticDataRecord anonymizedRecord = syntheticDataService.processAnonymization(id);
        return ResponseEntity.ok(convertToDTO(anonymizedRecord));
    }

    /**
     * GET /api/synthetic-data/statistics/generation-stats-by-method
     * Get generation statistics by method (Researcher/Admin only)
     */
    @GetMapping("/statistics/generation-stats-by-method")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getGenerationStatsByMethod() {
        List<Object[]> stats = syntheticDataService.getGenerationStatsByMethod();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/synthetic-data/statistics/generation-stats-by-cluster
     * Get generation statistics by cluster (Researcher/Admin only)
     */
    @GetMapping("/statistics/generation-stats-by-cluster")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getGenerationStatsByCluster() {
        List<Object[]> stats = syntheticDataService.getGenerationStatsByCluster();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/synthetic-data/statistics/daily-generation-counts
     * Get daily generation counts (Researcher/Admin only)
     */
    @GetMapping("/statistics/daily-generation-counts")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getDailyGenerationCounts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        List<Object[]> counts = syntheticDataService.getDailyGenerationCounts(startDate);
        return ResponseEntity.ok(counts);
    }

    /**
     * GET /api/synthetic-data/statistics/records-by-researcher
     * Get record counts by researcher (Researcher/Admin only)
     */
    @GetMapping("/statistics/records-by-researcher")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getRecordCountsByResearcher() {
        List<Object[]> counts = syntheticDataService.countRecordsByResearcher();
        return ResponseEntity.ok(counts);
    }

    /**
     * GET /api/synthetic-data/statistics/privacy-score-distribution
     * Get privacy score distribution (Researcher/Admin only)
     */
    @GetMapping("/statistics/privacy-score-distribution")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<Object[]> getPrivacyScoreDistribution() {
        Object[] distribution = syntheticDataService.getPrivacyScoreDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * GET /api/synthetic-data/statistics/average-scores-by-method
     * Get average quality scores by generation method (Researcher/Admin only)
     */
    @GetMapping("/statistics/average-scores-by-method")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getAverageScoresByMethod() {
        List<Object[]> averages = syntheticDataService.getAverageScoresByMethod();
        return ResponseEntity.ok(averages);
    }

    /**
     * GET /api/synthetic-data/statistics/total-records
     * Get total record count (Researcher/Admin only)
     */
    @GetMapping("/statistics/total-records")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<Long> getTotalRecordCount() {
        Long totalCount = syntheticDataService.getTotalRecordCount();
        return ResponseEntity.ok(totalCount);
    }

    /**
     * GET /api/synthetic-data/export-for-research
     * Export synthetic data for research purposes (Researcher/Admin only)
     */
    @GetMapping("/export-for-research")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<List<SyntheticDataRecordDTO>> exportForResearch(
            @RequestParam String researchPurpose,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<SyntheticDataRecord> records = syntheticDataService.exportForResearch(researchPurpose, startDate, endDate);
        List<SyntheticDataRecordDTO> recordDTOs = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordDTOs);
    }

    /**
     * GET /api/synthetic-data/quality-metrics-report
     * Generate quality metrics report (Researcher/Admin only)
     */
    @GetMapping("/quality-metrics-report")
    @PreAuthorize("hasRole('RESEARCHER') or hasRole('ADMIN')")
    public ResponseEntity<String> generateQualityMetricsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        String report = syntheticDataService.generateQualityMetricsReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * POST /api/synthetic-data/cleanup-old-records
     * Clean up old synthetic data records (Admin only)
     */
    @PostMapping("/cleanup-old-records")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupOldRecords(@RequestParam(defaultValue = "365") int daysOld) {
        syntheticDataService.cleanupOldRecords(daysOld);
        return ResponseEntity.ok(new ApiResponse(true, "Old synthetic data records cleaned up successfully"));
    }

    /**
     * DELETE /api/synthetic-data/{id}
     * Delete synthetic data record (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSyntheticDataRecord(@PathVariable Long id) {
        syntheticDataService.deleteById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Synthetic data record deleted successfully"));
    }

    // Helper methods
    private CustomUserDetailsService.UserPrincipal getCurrentUser(Authentication authentication) {
        return (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
    }

    private SyntheticDataRecordDTO convertToDTO(SyntheticDataRecord record) {
        SyntheticDataRecordDTO dto = new SyntheticDataRecordDTO();
        dto.setId(record.getId());
        dto.setRecordId(record.getRecordId());
        dto.setSourceUserCluster(record.getSourceUserCluster());
        dto.setGenerationMethod(record.getGenerationMethod().name());
        dto.setResearcherId(record.getResearcherId());
        dto.setResearchPurpose(record.getResearchPurpose());
        dto.setModelVersion(record.getModelVersion());
        dto.setPrivacyScore(record.getPrivacyScore());
        dto.setUtilityScore(record.getUtilityScore());
        dto.setIsValidated(record.getIsValidated());
        dto.setValidationScore(record.getValidationScore());
        dto.setGenerationTimestamp(record.getGenerationTimestamp());

        // Synthetic scores
        dto.setSyntheticStressScore(record.getSyntheticStressScore());
        dto.setSyntheticDepressionScore(record.getSyntheticDepressionScore());
        dto.setSyntheticAnxietyScore(record.getSyntheticAnxietyScore());
        dto.setSyntheticResilienceScore(record.getSyntheticResilienceScore());

        // Demographics
        dto.setAgeRange(record.getAgeRange());
        if (record.getGenderSynthetic() != null) {
            dto.setGenderSynthetic(record.getGenderSynthetic().name());
        }
        dto.setLocationRegion(record.getLocationRegion());

        // Behavioral patterns
        dto.setInteractionPatterns(record.getInteractionPatterns());
        dto.setAssessmentResponses(record.getAssessmentResponses());
        dto.setChatInteractionSynthetic(record.getChatInteractionSynthetic());

        return dto;
    }

    /**
     * Generic API Response class
     */
    public static class ApiResponse {
        private Boolean success;
        private String message;

        public ApiResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
