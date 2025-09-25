package com.research.mano.repository;

import com.research.mano.entity.SyntheticDataRecord;
import com.research.mano.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Synthetic Data Record Repository Interface (CORRECTED)
 * Handles CRUD operations for Component 1 (Privacy-Preserving Data Generation)
 */
@Repository
public interface SyntheticDataRecordRepository extends BaseRepository<SyntheticDataRecord, Long> {

    /**
     * Find synthetic record by record ID
     */
    Optional<SyntheticDataRecord> findByRecordId(String recordId);

    /**
     * Check if record ID exists
     */
    boolean existsByRecordId(String recordId);

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
    List<SyntheticDataRecord> findByIsValidatedTrue();

    /**
     * Find records by researcher ID
     */
    List<SyntheticDataRecord> findByResearcherId(String researcherId);

    /**
     * Find records for research purpose
     */
    List<SyntheticDataRecord> findByResearchPurposeContainingIgnoreCase(String purpose);

    /**
     * Find records by model version
     */
    List<SyntheticDataRecord> findByModelVersion(String modelVersion);

    /**
     * Find records by privacy score range
     */
    @Query("SELECT sdr FROM SyntheticDataRecord sdr WHERE sdr.privacyScore >= :minScore AND sdr.privacyScore <= :maxScore")
    List<SyntheticDataRecord> findByPrivacyScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Find records by utility score range
     */
    @Query("SELECT sdr FROM SyntheticDataRecord sdr WHERE sdr.utilityScore >= :minScore AND sdr.utilityScore <= :maxScore")
    List<SyntheticDataRecord> findByUtilityScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Find high-quality records (high utility, high privacy)
     */
    @Query("SELECT sdr FROM SyntheticDataRecord sdr WHERE sdr.privacyScore >= 0.8 AND sdr.utilityScore >= 0.8")
    List<SyntheticDataRecord> findHighQualityRecords();

    /**
     * Find records generated within the date range
     */
    @Query("SELECT sdr FROM SyntheticDataRecord sdr WHERE sdr.generationTimestamp BETWEEN :startDate AND :endDate")
    List<SyntheticDataRecord> findByGenerationDateRange(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find records by gender
     */
    List<SyntheticDataRecord> findByGenderSynthetic(User.Gender gender);

    /**
     * Find records by age range
     */
    List<SyntheticDataRecord> findByAgeRange(String ageRange);

    /**
     * Find records by location region
     */
    List<SyntheticDataRecord> findByLocationRegion(String region);

    /**
     * Get generation statistics by method
     */
    @Query("SELECT sdr.generationMethod, COUNT(sdr), AVG(sdr.privacyScore), AVG(sdr.utilityScore) " +
            "FROM SyntheticDataRecord sdr GROUP BY sdr.generationMethod")
    List<Object[]> getGenerationStatsByMethod();

    /**
     * Get generation statistics by cluster
     */
    @Query("SELECT sdr.sourceUserCluster, COUNT(sdr), AVG(sdr.privacyScore), AVG(sdr.utilityScore) " +
            "FROM SyntheticDataRecord sdr WHERE sdr.sourceUserCluster IS NOT NULL GROUP BY sdr.sourceUserCluster")
    List<Object[]> getGenerationStatsByCluster();

    /**
     * Get daily generation counts
     */
    @Query("SELECT DATE(sdr.generationTimestamp), COUNT(sdr) FROM SyntheticDataRecord sdr " +
            "WHERE sdr.generationTimestamp >= :startDate GROUP BY DATE(sdr.generationTimestamp)")
    List<Object[]> getDailyGenerationCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Find records needing validation
     */
    List<SyntheticDataRecord> findByIsValidatedFalse();

    /**
     * Get average scores by generation method
     */
    @Query("SELECT sdr.generationMethod, " +
            "AVG(sdr.syntheticStressScore), AVG(sdr.syntheticDepressionScore), AVG(sdr.syntheticAnxietyScore), AVG(sdr.syntheticResilienceScore) " +
            "FROM SyntheticDataRecord sdr GROUP BY sdr.generationMethod")
    List<Object[]> getAverageScoresByMethod();

    /**
     * Find recent records for research
     */
    @Query("SELECT sdr FROM SyntheticDataRecord sdr WHERE sdr.generationTimestamp >= :cutoffDate AND sdr.isValidated = true")
    List<SyntheticDataRecord> findRecentValidatedRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count records by researcher
     */
    @Query("SELECT sdr.researcherId, COUNT(sdr) FROM SyntheticDataRecord sdr GROUP BY sdr.researcherId")
    List<Object[]> countRecordsByResearcher();

    /**
     * Find records for specific research purposes
     */
    @Query("SELECT sdr FROM SyntheticDataRecord sdr WHERE sdr.researchPurpose IN :purposes AND sdr.isValidated = true")
    List<SyntheticDataRecord> findByResearchPurposes(@Param("purposes") List<String> purposes);

    /**
     * Get total generated record count
     */
    @Query("SELECT COUNT(sdr) FROM SyntheticDataRecord sdr")
    Long getTotalRecordCount();

    /**
     * Get privacy score distribution
     */
    @Query("SELECT " +
            "SUM(CASE WHEN sdr.privacyScore >= 0.8 THEN 1 ELSE 0 END) as high, " +
            "SUM(CASE WHEN sdr.privacyScore >= 0.6 AND sdr.privacyScore < 0.8 THEN 1 ELSE 0 END) as medium, " +
            "SUM(CASE WHEN sdr.privacyScore < 0.6 THEN 1 ELSE 0 END) as low " +
            "FROM SyntheticDataRecord sdr")
    Object[] getPrivacyScoreDistribution();

    /**
     * Find records with the best privacy-utility balance
     */
    @Query("SELECT sdr FROM SyntheticDataRecord sdr WHERE (sdr.privacyScore + sdr.utilityScore) / 2 >= :threshold ORDER BY (sdr.privacyScore + sdr.utilityScore) DESC")
    List<SyntheticDataRecord> findBestPrivacyUtilityBalance(@Param("threshold") Double threshold);
}