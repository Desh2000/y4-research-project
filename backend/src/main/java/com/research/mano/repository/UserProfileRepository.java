package com.research.mano.repository;


import com.research.mano.entity.UserProfile;
import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * User Profile Repository Interface
 * Handles CRUD operations for UserProfile entity with mental health data
 */
@Repository
public interface UserProfileRepository extends BaseRepository<UserProfile, Long> {

    /**
     * Find profile by user
     */
    Optional<UserProfile> findByUser(User user);

    /**
     * Find profile by user ID
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Check if profile exists for user
     */
    boolean existsByUserId(Long userId);

    /**
     * Find profiles by cluster group
     */
    List<UserProfile> findByCurrentClusterGroup(ClusterGroup clusterGroup);

    /**
     * Find profiles by cluster identifier
     */
    @Query("SELECT up FROM UserProfile up WHERE up.currentClusterGroup.clusterIdentifier = :identifier")
    List<UserProfile> findByClusterIdentifier(@Param("identifier") String identifier);

    /**
     * Find profiles with high risk alerts
     */
    List<UserProfile> findByHighRiskAlertTrue();

    /**
     * Find profiles requiring intervention
     */
    List<UserProfile> findByInterventionRequiredTrue();

    /**
     * Find profiles with synthetic data opt-in
     */
    List<UserProfile> findBySyntheticDataOptInTrue();

    /**
     * Find profiles with prediction alerts enabled
     */
    List<UserProfile> findByPredictionAlertsEnabledTrue();

    /**
     * Find profiles by stress score range (for Component 2 integration)
     */
    @Query("SELECT up FROM UserProfile up WHERE up.currentStressScore >= :minScore AND up.currentStressScore <= :maxScore")
    List<UserProfile> findByStressScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Find profiles by anxiety score range
     */
    @Query("SELECT up FROM UserProfile up WHERE up.currentAnxietyScore >= :minScore AND up.currentAnxietyScore <= :maxScore")
    List<UserProfile> findByAnxietyScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Find profiles by depression score range
     */
    @Query("SELECT up FROM UserProfile up WHERE up.currentDepressionScore >= :minScore AND up.currentDepressionScore <= :maxScore")
    List<UserProfile> findByDepressionScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * Find profiles with overall high risk (any score >= 0.8)
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
            "up.currentStressScore >= 0.8 OR up.currentAnxietyScore >= 0.8 OR up.currentDepressionScore >= 0.8")
    List<UserProfile> findHighRiskProfiles();

    /**
     * Find profiles with overall low risk (all scores <= 0.3)
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
            "up.currentStressScore <= 0.3 AND up.currentAnxietyScore <= 0.3 AND up.currentDepressionScore <= 0.3")
    List<UserProfile> findLowRiskProfiles();

    /**
     * Find profiles needing cluster reassignment (no cluster or old assignment)
     */
    @Query("SELECT up FROM UserProfile up WHERE up.currentClusterGroup IS NULL OR up.clusterAssignmentDate < :cutoffDate")
    List<UserProfile> findProfilesNeedingClusterReassignment(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find profiles with completed assessments
     */
    List<UserProfile> findByLastAssessmentDateIsNotNull();

    /**
     * Find profiles without recent assessments
     */
    @Query("SELECT up FROM UserProfile up WHERE up.lastAssessmentDate IS NULL OR up.lastAssessmentDate < :cutoffDate")
    List<UserProfile> findProfilesWithoutRecentAssessments(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find profiles in therapy
     */
    List<UserProfile> findByTherapyStartDateIsNotNull();

    /**
     * Find profiles by preferred language (for Component 3 - Chatbot)
     */
    List<UserProfile> findByPreferredLanguage(String language);

    /**
     * Find profiles with high chatbot interaction (for Component 3)
     */
    @Query("SELECT up FROM UserProfile up WHERE up.chatbotInteractionCount > :threshold")
    List<UserProfile> findActiveChatUsers(@Param("threshold") Integer threshold);

    /**
     * Get cluster distribution statistics
     */
    @Query("SELECT up.currentClusterGroup.clusterIdentifier, COUNT(up) " +
            "FROM UserProfile up WHERE up.currentClusterGroup IS NOT NULL " +
            "GROUP BY up.currentClusterGroup.clusterIdentifier")
    List<Object[]> getClusterDistributionStats();

    /**
     * Get average scores by cluster
     */
    @Query("SELECT up.currentClusterGroup.clusterIdentifier, " +
            "AVG(up.currentStressScore), AVG(up.currentAnxietyScore), AVG(up.currentDepressionScore) " +
            "FROM UserProfile up WHERE up.currentClusterGroup IS NOT NULL " +
            "GROUP BY up.currentClusterGroup.clusterIdentifier")
    List<Object[]> getAverageScoresByCluster();

    /**
     * Update current scores for a user profile
     */
    @Modifying
    @Query("UPDATE UserProfile up SET " +
            "up.currentStressScore = :stress, " +
            "up.currentAnxietyScore = :anxiety, " +
            "up.currentDepressionScore = :depression, " +
            "up.lastAssessmentDate = :assessmentDate " +
            "WHERE up.user.id = :userId")
    void updateCurrentScores(@Param("userId") Long userId,
                             @Param("stress") Double stress,
                             @Param("anxiety") Double anxiety,
                             @Param("depression") Double depression,
                             @Param("assessmentDate") LocalDateTime assessmentDate);

    /**
     * Update cluster assignment for a user profile
     */
    @Modifying
    @Query("UPDATE UserProfile up SET " +
            "up.previousClusterIdentifier = " +
            "(CASE WHEN up.currentClusterGroup IS NOT NULL THEN up.currentClusterGroup.clusterIdentifier ELSE NULL END), " +
            "up.currentClusterGroup = :clusterGroup, " +
            "up.clusterAssignmentDate = :assignmentDate " +
            "WHERE up.user.id = :userId")
    void updateClusterAssignment(@Param("userId") Long userId,
                                 @Param("clusterGroup") ClusterGroup clusterGroup,
                                 @Param("assignmentDate") LocalDateTime assignmentDate);

    /**
     * Clear high risk alerts for users with improved scores
     */
    @Modifying
    @Query("UPDATE UserProfile up SET up.highRiskAlert = false, up.interventionRequired = false " +
            "WHERE up.currentStressScore < 0.8 AND up.currentAnxietyScore < 0.8 AND up.currentDepressionScore < 0.8")
    void clearImprovedHighRiskAlerts();

    /**
     * Increment chatbot interaction count
     */
    @Modifying
    @Query("UPDATE UserProfile up SET up.chatbotInteractionCount = " +
            "(CASE WHEN up.chatbotInteractionCount IS NULL THEN 1 ELSE up.chatbotInteractionCount + 1 END) " +
            "WHERE up.user.id = :userId")
    void incrementChatbotInteraction(@Param("userId") Long userId);

    /**
     * Find profiles for ML model training data (Component 2)
     */
    @Query("SELECT up FROM UserProfile up WHERE " +
            "up.syntheticDataOptIn = true AND " +
            "up.lastAssessmentDate IS NOT NULL AND " +
            "up.currentStressScore IS NOT NULL AND " +
            "up.currentAnxietyScore IS NOT NULL AND " +
            "up.currentDepressionScore IS NOT NULL")
    List<UserProfile> findProfilesForMLTraining();
}
