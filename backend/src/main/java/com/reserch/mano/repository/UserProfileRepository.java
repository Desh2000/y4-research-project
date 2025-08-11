package com.reserch.mano.repository;

import com.reserch.mano.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // User profile lookup
    Optional<UserProfile> findByUserId(Long userId);

    // Consent tracking
    List<UserProfile> findByPrivacyConsentTrue();
    List<UserProfile> findByDataSharingConsentTrue();
    List<UserProfile> findByPrivacyConsentFalseOrDataSharingConsentFalse();

    // Mental health metrics queries
    @Query("SELECT p FROM UserProfile p WHERE p.stressLevel >= :threshold")
    List<UserProfile> findByHighStressLevel(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM UserProfile p WHERE p.anxietyLevel >= :threshold")
    List<UserProfile> findByHighAnxietyLevel(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM UserProfile p WHERE p.riskScore >= :threshold")
    List<UserProfile> findByHighRiskScore(@Param("threshold") Double threshold);

    @Query("SELECT p FROM UserProfile p WHERE p.resilienceScore <= :threshold")
    List<UserProfile> findByLowResilienceScore(@Param("threshold") Double threshold);

    // Statistics for ML models
    @Query("SELECT AVG(p.stressLevel) FROM UserProfile p WHERE p.stressLevel IS NOT NULL")
    Double getAverageStressLevel();

    @Query("SELECT AVG(p.anxietyLevel) FROM UserProfile p WHERE p.anxietyLevel IS NOT NULL")
    Double getAverageAnxietyLevel();

    @Query("SELECT AVG(p.resilienceScore) FROM UserProfile p WHERE p.resilienceScore IS NOT NULL")
    Double getAverageResilienceScore();

    @Query("SELECT AVG(p.riskScore) FROM UserProfile p WHERE p.riskScore IS NOT NULL")
    Double getAverageRiskScore();

    // Consent statistics
    long countByPrivacyConsentTrue();
    long countByDataSharingConsentTrue();
}
