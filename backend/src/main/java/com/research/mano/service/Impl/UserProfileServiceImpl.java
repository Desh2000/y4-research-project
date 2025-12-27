package com.research.mano.service.Impl;

import com.research.mano.entity.User;
import com.research.mano.entity.UserProfile;
import com.research.mano.entity.ClusterGroup;
import com.research.mano.repository.UserProfileRepository;
import com.research.mano.repository.ClusterGroupRepository;
import com.research.mano.service.UserProfileService;
import com.research.mano.service.SystemAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Profile Service Implementation
 * Handles extended user information and mental health data management
 */
@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ClusterGroupRepository clusterGroupRepository;

    @Autowired
    private SystemAlertService systemAlertService;

    @Override
    public UserProfile save(UserProfile userProfile) {
        return userProfileRepository.save(userProfile);
    }

    @Override
    public List<UserProfile> saveAll(List<UserProfile> userProfiles) {
        return userProfileRepository.saveAll(userProfiles);
    }

    @Override
    public Optional<UserProfile> findById(Long id) {
        return userProfileRepository.findById(id);
    }

    @Override
    public List<UserProfile> findAll() {
        return userProfileRepository.findAll();
    }

    @Override
    public Page<UserProfile> findAll(Pageable pageable) {
        return userProfileRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return userProfileRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        userProfileRepository.deleteById(id);
    }

    @Override
    public void delete(UserProfile userProfile) {
        userProfileRepository.delete(userProfile);
    }

    @Override
    public long count() {
        return userProfileRepository.count();
    }

    @Override
    public UserProfile createProfile(User user) {
        UserProfile profile = new UserProfile(user);

        // Set default values
        profile.setPreferredLanguage("en");
        profile.setPredictionAlertsEnabled(true);
        profile.setSyntheticDataOptIn(false);

        return userProfileRepository.save(profile);
    }

    @Override
    public Optional<UserProfile> findByUser(User user) {
        return userProfileRepository.findByUser(user);
    }

    @Override
    public Optional<UserProfile> findByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    @Override
    public UserProfile updateProfile(Long userId, UserProfile updatedProfile) {
        UserProfile existingProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        // Update allowed fields
        if (updatedProfile.getBio() != null) {
            existingProfile.setBio(updatedProfile.getBio());
        }
        if (updatedProfile.getLocation() != null) {
            existingProfile.setLocation(updatedProfile.getLocation());
        }
        if (updatedProfile.getTimezone() != null) {
            existingProfile.setTimezone(updatedProfile.getTimezone());
        }
        if (updatedProfile.getPreferredLanguage() != null) {
            existingProfile.setPreferredLanguage(updatedProfile.getPreferredLanguage());
        }
        if (updatedProfile.getNotificationPreferences() != null) {
            existingProfile.setNotificationPreferences(updatedProfile.getNotificationPreferences());
        }

        return userProfileRepository.save(existingProfile);
    }

    @Override
    public UserProfile updateCurrentScores(Long userId, Double stressScore, Double anxietyScore, Double depressionScore) {
        if (!validateScoreRanges(stressScore, anxietyScore, depressionScore)) {
            throw new IllegalArgumentException("Scores must be between 0.0 and 1.0");
        }

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.updateCurrentScores(stressScore, anxietyScore, depressionScore);

        // Check for high risk and generate alerts if needed
        if (isHighRisk(profile)) {
            systemAlertService.createHighRiskPredictionAlert(
                    profile.getUser(),
                    profile.getOverallRiskScore(),
                    String.format("Scores: Stress=%.2f, Anxiety=%.2f, Depression=%.2f",
                            stressScore, anxietyScore, depressionScore)
            );
        }

        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateResilienceScore(Long userId, Double resilienceScore) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setResilienceScore(resilienceScore);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile assignToCluster(Long userId, ClusterGroup clusterGroup) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        // Update cluster assignment
        profile.assignToCluster(clusterGroup);

        // Update cluster member counts
        if (profile.getCurrentClusterGroup() != null) {
            clusterGroupRepository.decrementMemberCount(
                    profile.getCurrentClusterGroup().getId(),
                    LocalDateTime.now()
            );
        }
        clusterGroupRepository.incrementMemberCount(clusterGroup.getId(), LocalDateTime.now());

        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile moveToCluster(Long userId, String newClusterIdentifier) {
        ClusterGroup newCluster = clusterGroupRepository.findByClusterIdentifier(newClusterIdentifier)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + newClusterIdentifier));

        return assignToCluster(userId, newCluster);
    }

    @Override
    public List<UserProfile> findByClusterGroup(ClusterGroup clusterGroup) {
        return userProfileRepository.findByCurrentClusterGroup(clusterGroup);
    }

    @Override
    public List<UserProfile> findByClusterIdentifier(String clusterIdentifier) {
        return userProfileRepository.findByClusterIdentifier(clusterIdentifier);
    }

    @Override
    public List<UserProfile> findHighRiskProfiles() {
        return userProfileRepository.findHighRiskProfiles();
    }

    @Override
    public List<UserProfile> findProfilesRequiringIntervention() {
        return userProfileRepository.findByInterventionRequiredTrue();
    }

    @Override
    public List<UserProfile> findSyntheticDataOptInProfiles() {
        return userProfileRepository.findBySyntheticDataOptInTrue();
    }

    @Override
    public List<UserProfile> findPredictionAlertsEnabledProfiles() {
        return userProfileRepository.findByPredictionAlertsEnabledTrue();
    }

    @Override
    public List<UserProfile> findByStressScoreRange(Double minScore, Double maxScore) {
        return userProfileRepository.findByStressScoreRange(minScore, maxScore);
    }

    @Override
    public List<UserProfile> findByAnxietyScoreRange(Double minScore, Double maxScore) {
        return userProfileRepository.findByAnxietyScoreRange(minScore, maxScore);
    }

    @Override
    public List<UserProfile> findByDepressionScoreRange(Double minScore, Double maxScore) {
        return userProfileRepository.findByDepressionScoreRange(minScore, maxScore);
    }

    @Override
    public List<UserProfile> findProfilesNeedingClusterReassignment(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return userProfileRepository.findProfilesNeedingClusterReassignment(cutoffDate);
    }

    @Override
    public List<UserProfile> findProfilesWithCompletedAssessments() {
        return userProfileRepository.findByLastAssessmentDateIsNotNull();
    }

    @Override
    public List<UserProfile> findProfilesWithoutRecentAssessments(int daysBack) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
        return userProfileRepository.findProfilesWithoutRecentAssessments(cutoffDate);
    }

    @Override
    public List<UserProfile> findProfilesInTherapy() {
        return userProfileRepository.findByTherapyStartDateIsNotNull();
    }

    @Override
    public List<UserProfile> findByPreferredLanguage(String language) {
        return userProfileRepository.findByPreferredLanguage(language);
    }

    @Override
    public List<UserProfile> findActiveChatUsers(Integer interactionThreshold) {
        return userProfileRepository.findActiveChatUsers(interactionThreshold);
    }

    @Override
    public List<Object[]> getClusterDistributionStats() {
        return userProfileRepository.getClusterDistributionStats();
    }

    @Override
    public List<Object[]> getAverageScoresByCluster() {
        return userProfileRepository.getAverageScoresByCluster();
    }

    @Override
    public UserProfile incrementChatbotInteraction(Long userId) {
        userProfileRepository.incrementChatbotInteraction(userId);
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    @Override
    public UserProfile updateTherapyInfo(Long userId, LocalDateTime therapyStartDate, String medications, String goals) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setTherapyStartDate(therapyStartDate);
        profile.setCurrentMedications(medications);
        profile.setMentalHealthGoals(goals);

        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateCrisisInterventionPlan(Long userId, String plan) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setCrisisInterventionPlan(plan);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateEmergencyContact(Long userId, String contactName, String contactPhone) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        // Update emergency contact in User entity
        profile.getUser().setEmergencyContactName(contactName);
        profile.getUser().setEmergencyContactPhone(contactPhone);

        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateNotificationPreferences(Long userId, String preferences) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setNotificationPreferences(preferences);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateLocationInfo(Long userId, String location, String timezone) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setLocation(location);
        profile.setTimezone(timezone);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile setHighRiskAlert(Long userId, boolean highRisk) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setHighRiskAlert(highRisk);
        if (highRisk) {
            profile.setLastHighRiskDate(LocalDateTime.now());
            profile.setInterventionRequired(true);
        }

        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile clearHighRiskAlert(Long userId) {
        return setHighRiskAlert(userId, false);
    }

    @Override
    public UserProfile setInterventionRequired(Long userId, boolean required) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setInterventionRequired(required);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateClusterStabilityScore(Long userId, Double stabilityScore) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setClusterStabilityScore(stabilityScore);
        return userProfileRepository.save(profile);
    }

    @Override
    public Double calculateOverallRiskScore(UserProfile profile) {
        return profile.getOverallRiskScore();
    }

    @Override
    public boolean isHighRisk(UserProfile profile) {
        Double overallRisk = calculateOverallRiskScore(profile);
        return overallRisk != null && overallRisk >= 0.8;
    }

    @Override
    public String getCurrentClusterIdentifier(UserProfile profile) {
        return profile.getCurrentClusterIdentifier();
    }

    @Override
    public UserProfile updateSyntheticDataSettings(Long userId, boolean optIn) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setSyntheticDataOptIn(optIn);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updatePredictionSettings(Long userId, boolean alertsEnabled) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setPredictionAlertsEnabled(alertsEnabled);
        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile updateSupportNetwork(Long userId, Integer networkSize) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setSupportNetworkSize(networkSize);
        return userProfileRepository.save(profile);
    }

    @Override
    public List<UserProfile> findProfilesForMLTraining() {
        return userProfileRepository.findProfilesForMLTraining();
    }

    @Override
    public void clearImprovedHighRiskAlerts() {
        userProfileRepository.clearImprovedHighRiskAlerts();
    }

    @Override
    public UserProfile processAssessmentCompletion(Long userId, Double stressScore, Double anxietyScore, Double depressionScore) {
        return updateCurrentScores(userId, stressScore, anxietyScore, depressionScore);
    }

    @Override
    public String generateProfileSummary(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        StringBuilder summary = new StringBuilder();
        summary.append("User Profile Summary:\n");
        summary.append("Current Risk Level: ").append(isHighRisk(profile) ? "High" : "Normal").append("\n");
        summary.append("Cluster: ").append(getCurrentClusterIdentifier(profile)).append("\n");

        if (profile.getCurrentStressScore() != null) {
            summary.append("Stress Score: ").append(String.format("%.2f", profile.getCurrentStressScore())).append("\n");
        }
        if (profile.getCurrentAnxietyScore() != null) {
            summary.append("Anxiety Score: ").append(String.format("%.2f", profile.getCurrentAnxietyScore())).append("\n");
        }
        if (profile.getCurrentDepressionScore() != null) {
            summary.append("Depression Score: ").append(String.format("%.2f", profile.getCurrentDepressionScore())).append("\n");
        }

        return summary.toString();
    }

    @Override
    public Double calculateResilienceScore(UserProfile profile) {
        // Implement resilience calculation logic based on profile data
        if (profile.getCurrentStressScore() == null ||
                profile.getCurrentAnxietyScore() == null ||
                profile.getCurrentDepressionScore() == null) {
            return null;
        }

        // Simple resilience calculation (inverse of average risk)
        Double avgRisk = calculateOverallRiskScore(profile);
        return avgRisk != null ? (1.0 - avgRisk) : null;
    }

    @Override
    public UserProfile updateBio(Long userId, String bio) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setBio(bio);
        return userProfileRepository.save(profile);
    }

    @Override
    public boolean validateScoreRanges(Double stressScore, Double anxietyScore, Double depressionScore) {
        return isValidScore(stressScore) && isValidScore(anxietyScore) && isValidScore(depressionScore);
    }

    private boolean isValidScore(Double score) {
        return score != null && score >= 0.0 && score <= 1.0;
    }

    @Override
    public List<UserProfile> findByMentalHealthGoals(String goals) {
        return userProfileRepository.findAll().stream()
                .filter(profile -> profile.getMentalHealthGoals() != null &&
                        profile.getMentalHealthGoals().toLowerCase().contains(goals.toLowerCase()))
                .toList();
    }

    @Override
    public void archiveInactiveProfiles(int monthsInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(monthsInactive);

        List<UserProfile> inactiveProfiles = userProfileRepository.findAll().stream()
                .filter(profile -> profile.getLastAssessmentDate() != null &&
                        profile.getLastAssessmentDate().isBefore(cutoffDate))
                .toList();

        // Archive logic here - could move to separate archive table
        // For now, just mark as inactive in user entity
        inactiveProfiles.forEach(profile -> profile.getUser().setIsActive(false));
        userProfileRepository.saveAll(inactiveProfiles);
    }
}