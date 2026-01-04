package com.research.mano.util;

import com.research.mano.controller.responce.*;
import com.research.mano.dto.chat.ChatConversationDTO;
import com.research.mano.dto.cluster.ClusterGroupDTO;
import com.research.mano.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity to DTO Mapper Utility
 * Provides clean mapping between entities and DTOs
 */
@Component
public class EntityDTOMapper {

    // User mappings
    public UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null) return null;

        UserSummaryDTO dto = new UserSummaryDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setEmailVerified(user.getIsEmailVerified());

        // Set roles
        String[] roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toArray(String[]::new);
        dto.setRoles(roles);

        if (user.getMentalHealthStatus() != null) {
            dto.setMentalHealthStatus(user.getMentalHealthStatus().name());
        }

        return dto;
    }

    // Mental Health Prediction mappings
    public MentalHealthPredictionDTO toPredictionDTO(MentalHealthPrediction prediction) {
        if (prediction == null) return null;

        MentalHealthPredictionDTO dto = new MentalHealthPredictionDTO();
        dto.setId(prediction.getId());
        dto.setUserId(prediction.getUser().getId());
        dto.setUsername(prediction.getUser().getUsername());
        dto.setStressScore(prediction.getStressScore());
        dto.setDepressionScore(prediction.getDepressionScore());
        dto.setAnxietyScore(prediction.getAnxietyScore());
        dto.setOverallRiskScore(prediction.getOverallRiskScore());
        dto.setPredictionDate(prediction.getPredictionDate());
        dto.setModelVersion(prediction.getModelVersion());
        dto.setDataSource(prediction.getDataSource());

        if (prediction.getPrimaryClusterCategory() != null) {
            dto.setPrimaryClusterCategory(prediction.getPrimaryClusterCategory().name());
        }
        if (prediction.getPrimaryClusterLevel() != null) {
            dto.setPrimaryClusterLevel(prediction.getPrimaryClusterLevel().name());
        }
        dto.setClusterAssignmentDate(prediction.getClusterAssignmentDate());

        return dto;
    }

    public List<MentalHealthPredictionDTO> toPredictionDTOList(List<MentalHealthPrediction> predictions) {
        return predictions.stream()
                .map(this::toPredictionDTO)
                .collect(Collectors.toList());
    }

    // User Profile mappings
    public UserProfileDTO toUserProfileDTO(UserProfile profile) {
        if (profile == null) return null;

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

    // Cluster Group mappings
    public ClusterGroupDTO toClusterGroupDTO(ClusterGroup cluster) {
        if (cluster == null) return null;

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
        dto.setMemberCount(cluster.getMemberCount());
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

    public List<ClusterGroupDTO> toClusterGroupDTOList(List<ClusterGroup> clusters) {
        return clusters.stream()
                .map(this::toClusterGroupDTO)
                .collect(Collectors.toList());
    }

    // Chat Conversation mappings
    public ChatConversationDTO toChatConversationDTO(ChatConversation conversation) {
        if (conversation == null) return null;

        ChatConversationDTO dto = new ChatConversationDTO();
        dto.setId(conversation.getId());
        dto.setUserId(conversation.getUser().getId());
        dto.setUsername(conversation.getUser().getUsername());
        dto.setSessionId(conversation.getSessionId());
        dto.setMessageText(conversation.getMessageText());
        if (conversation.getMessageType() != null) {
            dto.setMessageType(conversation.getMessageType().name());
        }
        dto.setSentimentScore(conversation.getSentimentScore());
        dto.setEmotionDetected(conversation.getEmotionDetected());
        dto.setCrisisKeywordsDetected(conversation.getCrisisKeywordsDetected());
        dto.setInterventionTriggered(conversation.getInterventionTriggered());
        dto.setResponseTimeMs(conversation.getResponseTimeMs());
        dto.setModelVersion(conversation.getModelVersion());
        dto.setContextData(conversation.getContextData());
        dto.setCreatedAt(conversation.getCreatedAt());

        return dto;
    }

    public List<ChatConversationDTO> toChatConversationDTOList(List<ChatConversation> conversations) {
        return conversations.stream()
                .map(this::toChatConversationDTO)
                .collect(Collectors.toList());
    }

    // System Alert mappings
    public SystemAlertDTO toSystemAlertDTO(SystemAlert alert) {
        if (alert == null) return null;

        SystemAlertDTO dto = new SystemAlertDTO();
        dto.setId(alert.getId());
        if (alert.getUser() != null) {
            dto.setUserId(alert.getUser().getId());
            dto.setUsername(alert.getUser().getUsername());
        }
        if (alert.getAlertType() != null) {
            dto.setAlertType(alert.getAlertType().name());
        }
        if (alert.getSeverityLevel() != null) {
            dto.setSeverityLevel(alert.getSeverityLevel().name());
        }
        dto.setTitle(alert.getAlertTitle());
        dto.setMessage(alert.getAlertMessage());
        dto.setTriggerSource(alert.getTriggerSource());
        dto.setTriggerData(alert.getTriggerData());
        dto.setIsCrisis(alert.getIsCrisis());
        dto.setIsResolved(alert.getIsResolved());
        dto.setAssignedTo(alert.getAssignedTo());
        dto.setEmergencyContactNotified(alert.getEmergencyContactNotified());
        dto.setProfessionalNotified(alert.getProfessionalNotified());
        dto.setActionTaken(alert.getActionTaken());
        dto.setResolvedBy(alert.getResolvedBy());
        dto.setResolutionNotes(alert.getResolutionNotes());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setNotificationSentAt(alert.getNotificationSentAt());

        return dto;
    }

    public List<SystemAlertDTO> toSystemAlertDTOList(List<SystemAlert> alerts) {
        return alerts.stream()
                .map(this::toSystemAlertDTO)
                .collect(Collectors.toList());
    }

    // Synthetic Data Record mappings
    public SyntheticDataRecordDTO toSyntheticDataRecordDTO(SyntheticDataRecord record) {
        if (record == null) return null;

        SyntheticDataRecordDTO dto = new SyntheticDataRecordDTO();
        dto.setId(record.getId());
        dto.setRecordId(record.getRecordId());
        dto.setSourceUserCluster(record.getSourceUserCluster());
        if (record.getGenerationMethod() != null) {
            dto.setGenerationMethod(record.getGenerationMethod().name());
        }
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

    public List<SyntheticDataRecordDTO> toSyntheticDataRecordDTOList(List<SyntheticDataRecord> records) {
        return records.stream()
                .map(this::toSyntheticDataRecordDTO)
                .collect(Collectors.toList());
    }
}
