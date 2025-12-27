package com.research.mano.controller;


import com.research.mano.controller.request.*;
import com.research.mano.controller.responce.*;
import com.research.mano.entity.User;
import com.research.mano.entity.UserProfile;
import com.research.mano.service.UserProfileService;
import com.research.mano.service.UserService;
import com.research.mano.service.Impl.CustomUserDetailsService;
import com.research.mano.exception.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Profile Controller
 * Handles user mental health profiles and cluster management
 */
@RestController
@RequestMapping("/api/mental-health/profiles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    /**
     * GET /api/mental-health/profiles/me
     * Get current user's profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);

        UserProfile profile = userProfileService.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new UserProfileNotFoundException(userPrincipal.getId()));

        return ResponseEntity.ok(convertToDTO(profile));
    }

    /**
     * PUT /api/mental-health/profiles/me
     * Update current user's profile
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<UserProfileDTO> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileDTO profileRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);

        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setBio(profileRequest.getBio());
        updatedProfile.setLocation(profileRequest.getLocation());
        updatedProfile.setTimezone(profileRequest.getTimezone());
        updatedProfile.setPreferredLanguage(profileRequest.getPreferredLanguage());
        updatedProfile.setMentalHealthGoals(profileRequest.getMentalHealthGoals());
        updatedProfile.setCrisisInterventionPlan(profileRequest.getCrisisInterventionPlan());
        updatedProfile.setSupportNetworkSize(profileRequest.getSupportNetworkSize());

        UserProfile savedProfile = userProfileService.updateProfile(userPrincipal.getId(), updatedProfile);
        return ResponseEntity.ok(convertToDTO(savedProfile));
    }

    /**
     * PUT /api/mental-health/profiles/me/scores
     * Update current mental health scores
     */
    @PutMapping("/me/scores")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<UserProfileDTO> updateMentalHealthScores(
            @Valid @RequestBody MentalHealthScoreUpdateRequest scoreRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);

        UserProfile updatedProfile = userProfileService.updateCurrentScores(
                userPrincipal.getId(),
                scoreRequest.getStressScore(),
                scoreRequest.getAnxietyScore(),
                scoreRequest.getDepressionScore()
        );

        return ResponseEntity.ok(convertToDTO(updatedProfile));
    }

    /**
     * GET /api/mental-health/profiles/{userId}
     * Get user profile by ID (Healthcare Professional/Admin only)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        UserProfile profile = userProfileService.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException(userId));

        return ResponseEntity.ok(convertToDTO(profile));
    }

    /**
     * GET /api/mental-health/profiles/high-risk
     * Get all high-risk user profiles (Healthcare Professional/Admin only)
     */
    @GetMapping("/high-risk")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> getHighRiskProfiles() {
        List<UserProfile> profiles = userProfileService.findHighRiskProfiles();

        List<UserProfileDTO> profileDTOs = profiles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(profileDTOs);
    }

    /**
     * GET /api/mental-health/profiles/requiring-intervention
     * Get profiles requiring intervention (Healthcare Professional/Admin only)
     */
    @GetMapping("/requiring-intervention")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> getProfilesRequiringIntervention() {
        List<UserProfile> profiles = userProfileService.findProfilesRequiringIntervention();

        List<UserProfileDTO> profileDTOs = profiles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(profileDTOs);
    }

    /**
     * GET /api/mental-health/profiles/cluster/{clusterIdentifier}
     * Get profiles by cluster (Healthcare Professional/Admin only)
     */
    @GetMapping("/cluster/{clusterIdentifier}")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> getProfilesByCluster(@PathVariable String clusterIdentifier) {
        List<UserProfile> profiles = userProfileService.findByClusterIdentifier(clusterIdentifier);

        List<UserProfileDTO> profileDTOs = profiles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(profileDTOs);
    }

    /**
     * POST /api/mental-health/profiles/{userId}/move-cluster
     * Move user to different cluster (Healthcare Professional/Admin only)
     */
    @PostMapping("/{userId}/move-cluster")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> moveUserToCluster(
            @PathVariable Long userId,
            @RequestParam String clusterIdentifier) {

        UserProfile updatedProfile = userProfileService.moveToCluster(userId, clusterIdentifier);
        return ResponseEntity.ok(convertToDTO(updatedProfile));
    }

    /**
     * PUT /api/mental-health/profiles/{userId}/high-risk
     * Set high-risk alert for user (Healthcare Professional/Admin only)
     */
    @PutMapping("/{userId}/high-risk")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> setHighRiskAlert(
            @PathVariable Long userId,
            @RequestParam boolean highRisk) {

        UserProfile updatedProfile = userProfileService.setHighRiskAlert(userId, highRisk);
        return ResponseEntity.ok(convertToDTO(updatedProfile));
    }

    /**
     * PUT /api/mental-health/profiles/{userId}/intervention-required
     * Set intervention required flag (Healthcare Professional/Admin only)
     */
    @PutMapping("/{userId}/intervention-required")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> setInterventionRequired(
            @PathVariable Long userId,
            @RequestParam boolean required) {

        UserProfile updatedProfile = userProfileService.setInterventionRequired(userId, required);
        return ResponseEntity.ok(convertToDTO(updatedProfile));
    }

    /**
     * GET /api/mental-health/profiles/statistics/cluster-distribution
     * Get cluster distribution statistics (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/cluster-distribution")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getClusterDistributionStats() {
        List<Object[]> stats = userProfileService.getClusterDistributionStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/mental-health/profiles/statistics/average-scores-by-cluster
     * Get average scores by cluster (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/average-scores-by-cluster")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getAverageScoresByCluster() {
        List<Object[]> stats = userProfileService.getAverageScoresByCluster();
        return ResponseEntity.ok(stats);
    }

    /**
     * PUT /api/mental-health/profiles/me/settings
     * Update profile settings (privacy, notifications)
     */
    @PutMapping("/me/settings")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<UserProfileDTO> updateProfileSettings(
            @RequestParam(required = false) Boolean syntheticDataOptIn,
            @RequestParam(required = false) Boolean predictionAlertsEnabled,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        UserProfile profile = userProfileService.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new UserProfileNotFoundException(userPrincipal.getId()));

        if (syntheticDataOptIn != null) {
            profile = userProfileService.updateSyntheticDataSettings(userPrincipal.getId(), syntheticDataOptIn);
        }

        if (predictionAlertsEnabled != null) {
            profile = userProfileService.updatePredictionSettings(userPrincipal.getId(), predictionAlertsEnabled);
        }

        return ResponseEntity.ok(convertToDTO(profile));
    }

    /**
     * GET /api/mental-health/profiles/me/summary
     * Get profile summary
     */
    @GetMapping("/me/summary")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<String> getProfileSummary(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        String summary = userProfileService.generateProfileSummary(userPrincipal.getId());
        return ResponseEntity.ok(summary);
    }

    /**
     * POST /api/mental-health/profiles/me/assessment-completion
     * Process assessment completion
     */
    @PostMapping("/me/assessment-completion")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<UserProfileDTO> processAssessmentCompletion(
            @Valid @RequestBody MentalHealthScoreUpdateRequest scoreRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);

        UserProfile updatedProfile = userProfileService.processAssessmentCompletion(
                userPrincipal.getId(),
                scoreRequest.getStressScore(),
                scoreRequest.getAnxietyScore(),
                scoreRequest.getDepressionScore()
        );

        return ResponseEntity.ok(convertToDTO(updatedProfile));
    }

    // Helper methods
    private CustomUserDetailsService.UserPrincipal getCurrentUser(Authentication authentication) {
        return (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
    }

    private UserProfileDTO convertToDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUser().getId());
        dto.setBio(profile.getBio());
        dto.setLocation(profile.getLocation());
        dto.setTimezone(profile.getTimezone());
        dto.setPreferredLanguage(profile.getPreferredLanguage());

        // Mental health scores
        dto.setCurrentStressScore(profile.getCurrentStressScore());
        dto.setCurrentAnxietyScore(profile.getCurrentAnxietyScore());
        dto.setCurrentDepressionScore(profile.getCurrentDepressionScore());
        dto.setResilienceScore(profile.getResilienceScore());
        dto.setLastAssessmentDate(profile.getLastAssessmentDate());

        // Therapy information
        dto.setTherapyStartDate(profile.getTherapyStartDate());
        dto.setCurrentMedications(profile.getCurrentMedications());
        dto.setMentalHealthGoals(profile.getMentalHealthGoals());
        dto.setCrisisInterventionPlan(profile.getCrisisInterventionPlan());
        dto.setSupportNetworkSize(profile.getSupportNetworkSize());

        // Cluster information
        dto.setCurrentClusterIdentifier(profile.getCurrentClusterIdentifier());
        dto.setClusterAssignmentDate(profile.getClusterAssignmentDate());
        dto.setPreviousClusterIdentifier(profile.getPreviousClusterIdentifier());
        dto.setClusterStabilityScore(profile.getClusterStabilityScore());

        // Risk flags
        dto.setHighRiskAlert(profile.getHighRiskAlert());
        dto.setLastHighRiskDate(profile.getLastHighRiskDate());
        dto.setInterventionRequired(profile.getInterventionRequired());

        // Settings
        dto.setSyntheticDataOptIn(profile.getSyntheticDataOptIn());
        dto.setPredictionAlertsEnabled(profile.getPredictionAlertsEnabled());
        dto.setChatbotInteractionCount(profile.getChatbotInteractionCount());

        return dto;
    }
}
