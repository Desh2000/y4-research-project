package com.research.mano.service;


import com.research.mano.entity.SyntheticDataRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Synthetic Data Record Service Interface
 * Business logic for Component 1 (Privacy-Preserving Data Generation System)
 */
public interface SyntheticDataRecordService extends BaseService<SyntheticDataRecord, Long> {

    /**
     * Generate synthetic data record
     */
    SyntheticDataRecord generateSyntheticRecord(String sourceUserCluster,
                                                SyntheticDataRecord.GenerationMethod method,
                                                String researcherId, String researchPurpose);

    /**
     * Generate a batch of synthetic records
     */
    List<SyntheticDataRecord> generateBatchSyntheticRecords(String sourceUserCluster,
                                                            SyntheticDataRecord.GenerationMethod method,
                                                            int batchSize, String researcherId,
                                                            String researchPurpose);

    /**
     * Find synthetic record by record ID
     */
    Optional<SyntheticDataRecord> findByRecordId(String recordId);

    /**
     * Find records by generation method
     */
    List<SyntheticDataRecord> findByGenerationMethod(SyntheticDataRecord.GenerationMethod method);

    /**
     * Find records by source cluster
     */
    List<SyntheticDataRecord> findBySourceUserCluster(String clusterIdentifier);

    /**
     * Find validated records
     */
    List<SyntheticDataRecord> findValidatedRecords();

    /**
     * Find records needing validation
     */
    List<SyntheticDataRecord> findRecordsNeedingValidation();

    /**
     * Find records by researcher
     */
    List<SyntheticDataRecord> findByResearcher(String researcherId);

    /**
     * Find records by research purpose
     */
    List<SyntheticDataRecord> findByResearchPurpose(String purpose);

    /**
     * Find high-quality records (high privacy + utility scores)
     */
    List<SyntheticDataRecord> findHighQualityRecords();

    /**
     * Find records by privacy score range
     */
    List<SyntheticDataRecord> findByPrivacyScoreRange(Double minScore, Double maxScore);

    /**
     * Find records by utility score range
     */
    List<SyntheticDataRecord> findByUtilityScoreRange(Double minScore, Double maxScore);

    /**
     * Find records generated within the date range
     */
    List<SyntheticDataRecord> findByGenerationDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent validated records for research
     */
    List<SyntheticDataRecord> findRecentValidatedRecords(int daysBack);

    /**
     * Validate synthetic data record
     */
    SyntheticDataRecord validateRecord(Long recordId, Double validationScore);

    /**
     * Update privacy and utility scores
     */
    SyntheticDataRecord updateQualityScores(Long recordId, Double privacyScore, Double utilityScore);

    /**
     * Update synthetic mental health scores
     */
    SyntheticDataRecord updateSyntheticScores(Long recordId, Double stressScore,
                                              Double depressionScore, Double anxietyScore,
                                              Double resilienceScore);

    /**
     * Calculate privacy score for record
     */
    Double calculatePrivacyScore(SyntheticDataRecord record);

    /**
     * Calculate utility score for record
     */
    Double calculateUtilityScore(SyntheticDataRecord record);

    /**
     * Generate unique record ID
     */
    String generateRecordId(String prefix);

    /**
     * Get generation statistics by method
     */
    List<Object[]> getGenerationStatsByMethod();

    /**
     * Get generation statistics by cluster
     */
    List<Object[]> getGenerationStatsByCluster();

    /**
     * Get daily generation counts
     */
    List<Object[]> getDailyGenerationCounts(LocalDateTime startDate);

    /**
     * Count records by researcher
     */
    List<Object[]> countRecordsByResearcher();

    /**
     * Find records for specific research purposes
     */
    List<SyntheticDataRecord> findByResearchPurposes(List<String> purposes);

    /**
     * Get total record count
     */
    Long getTotalRecordCount();

    /**
     * Get privacy score distribution
     */
    Object[] getPrivacyScoreDistribution();

    /**
     * Find best privacy-utility balance records
     */
    List<SyntheticDataRecord> findBestPrivacyUtilityBalance(Double threshold);

    /**
     * Get average scores by generation method
     */
    List<Object[]> getAverageScoresByMethod();

    /**
     * Process anonymization for record
     */
    SyntheticDataRecord processAnonymization(Long recordId);

    /**
     * Generate synthetic demographic data
     */
    void generateSyntheticDemographics(SyntheticDataRecord record);

    /**
     * Generate synthetic behavioral patterns
     */
    void generateSyntheticBehavioralPatterns(SyntheticDataRecord record);

    /**
     * Generate synthetic interaction patterns
     */
    void generateSyntheticInteractionPatterns(SyntheticDataRecord record);

    /**
     * Validate data privacy compliance
     */
    boolean validatePrivacyCompliance(SyntheticDataRecord record);

    /**
     * Export records for research (anonymized)
     */
    List<SyntheticDataRecord> exportForResearch(String researchPurpose, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Clean up old synthetic records
     */
    void cleanupOldRecords(int daysOld);

    /**
     * Generate quality metrics report
     */
    String generateQualityMetricsReport(LocalDateTime startDate, LocalDateTime endDate);
}