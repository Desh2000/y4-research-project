package com.research.mano.service;


import com.research.mano.entity.User;
import com.research.mano.entity.UserProfile;
import com.research.mano.entity.ClusterGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Profile Service Interface
 * Business logic for extended user information and mental health data
 */
public interface UserProfileService extends BaseService<UserProfile, Long> {

    /**
     * Create new user profile
     */
    UserProfile createProfile(User user);

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
     * Update user profile information
     */
    UserProfile updateProfile(Long userId, UserProfile updatedProfile);

    /**
     * Update current mental health scores
     */
    UserProfile updateCurrentScores(Long userId, Double stressScore, Double anxietyScore,
                                    Double depressionScore);

    /**
     * Update resilience score
     */
    UserProfile updateResilienceScore(Long userId, Double resilienceScore);

    /**
     * Assign user to cluster
     */
    UserProfile assignToCluster(Long userId, ClusterGroup clusterGroup);

    /**
     * Move user between clusters
     */
    UserProfile moveToCluster(Long userId, String newClusterIdentifier);

    /**
     * Find profiles by cluster group
     */
    List<UserProfile> findByClusterGroup(ClusterGroup clusterGroup);

    /**
     * Find profiles by cluster identifier
     */
    List<UserProfile> findByClusterIdentifier(String clusterIdentifier);

    /**
     * Find profiles with high risk alerts
     */
    List<UserProfile> findHighRiskProfiles();

    /**
     * Find profiles requiring intervention
     */
    List<UserProfile> findProfilesRequiringIntervention();

    /**
     * Find profiles with synthetic data opt-in
     */
    List<UserProfile> findSyntheticDataOptInProfiles();

    /**
     * Find profiles with prediction alerts enabled
     */
    List<UserProfile> findPredictionAlertsEnabledProfiles();

    /**
     * Find profiles by score ranges
     */
    List<UserProfile> findByStressScoreRange(Double minScore, Double maxScore);
    List<UserProfile> findByAnxietyScoreRange(Double minScore, Double maxScore);
    List<UserProfile> findByDepressionScoreRange(Double minScore, Double maxScore);

    /**
     * Find profiles needing cluster reassignment
     */
    List<UserProfile> findProfilesNeedingClusterReassignment(int daysOld);

    /**
     * Find profiles with completed assessments
     */
    List<UserProfile> findProfilesWithCompletedAssessments();

    /**
     * Find profiles without recent assessments
     */
    List<UserProfile> findProfilesWithoutRecentAssessments(int daysBack);

    /**
     * Find profiles in therapy
     */
    List<UserProfile> findProfilesInTherapy();

    /**
     * Find profiles by preferred language
     */
    List<UserProfile> findByPreferredLanguage(String language);

    /**
     * Find active chat users
     */
    List<UserProfile> findActiveChatUsers(Integer interactionThreshold);

    /**
     * Get cluster distribution statistics
     */
    List<Object[]> getClusterDistributionStats();

    /**
     * Get average scores by cluster
     */
    List<Object[]> getAverageScoresByCluster();

    /**
     * Update chatbot interaction count
     */
    UserProfile incrementChatbotInteraction(Long userId);

    /**
     * Update therapy information
     */
    UserProfile updateTherapyInfo(Long userId, LocalDateTime therapyStartDate,
                                  String medications, String goals);

    /**
     * Update crisis intervention plan
     */
    UserProfile updateCrisisInterventionPlan(Long userId, String plan);

    /**
     * Update emergency contact information
     */
    UserProfile updateEmergencyContact(Long userId, String contactName, String contactPhone);

    /**
     * Update notification preferences
     */
    UserProfile updateNotificationPreferences(Long userId, String preferences);

    /**
     * Update location and timezone
     */
    UserProfile updateLocationInfo(Long userId, String location, String timezone);

    /**
     * Set high risk alert
     */
    UserProfile setHighRiskAlert(Long userId, boolean highRisk);

    /**
     * Clear high risk alert
     */
    UserProfile clearHighRiskAlert(Long userId);

    /**
     * Set intervention required flag
     */
    UserProfile setInterventionRequired(Long userId, boolean required);

    /**
     * Update cluster stability score
     */
    UserProfile updateClusterStabilityScore(Long userId, Double stabilityScore);

    /**
     * Calculate overall risk score
     */
    Double calculateOverallRiskScore(UserProfile profile);

    /**
     * Check if user is high risk (any score >= 0.8)
     */
    boolean isHighRisk(UserProfile profile);

    /**
     * Get current cluster identifier
     */
    String getCurrentClusterIdentifier(UserProfile profile);

    /**
     * Update profile settings for Component 1 (Synthetic Data)
     */
    UserProfile updateSyntheticDataSettings(Long userId, boolean optIn);

    /**
     * Update profile settings for Component 2 (Prediction)
     */
    UserProfile updatePredictionSettings(Long userId, boolean alertsEnabled);

    /**
     * Update support network information
     */
    UserProfile updateSupportNetwork(Long userId, Integer networkSize);

    /**
     * Find profiles for ML training data
     */
    List<UserProfile> findProfilesForMLTraining();

    /**
     * Clear improved high risk alerts (batch operation)
     */
    void clearImprovedHighRiskAlerts();

    /**
     * Process assessment completion
     */
    UserProfile processAssessmentCompletion(Long userId, Double stressScore,
                                            Double anxietyScore, Double depressionScore);

    /**
     * Generate profile summary
     */
    String generateProfileSummary(Long userId);

    /**
     * Calculate resilience score based on profile data
     */
    Double calculateResilienceScore(UserProfile profile);

    /**
     * Update bio information
     */
    UserProfile updateBio(Long userId, String bio);

    /**
     * Validate score ranges (0.0-1.0)
     */
    boolean validateScoreRanges(Double stressScore, Double anxietyScore, Double depressionScore);

    /**
     * Get profiles by mental health goals
     */
    List<UserProfile> findByMentalHealthGoals(String goals);

    /**
     * Archive inactive profiles
     */
    void archiveInactiveProfiles(int monthsInactive);
}