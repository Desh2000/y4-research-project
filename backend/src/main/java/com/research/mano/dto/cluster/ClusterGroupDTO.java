package com.research.mano.dto.cluster;

import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.ClusterGroup.ClusterCategory;
import com.research.mano.entity.ClusterGroup.SeverityLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for Cluster Group
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterGroupDTO {

    private Long id;
    private String clusterIdentifier;
    private String clusterName;
    private String description;

    // Classification
    private String category;
    private String primaryCategoryDisplayName;
    private String level;
    private String severityLevelDisplayName;
    private Integer clusterIndex;

    // Centroids
    private Double centroidStress;
    private Double centroidDepression;
    private Double centroidAnxiety;
    private Double centroidResilience;

    // Member Statistics
    private Long memberCount;
    private Long activeMemberCount;
    private Integer maxCapacity;
    private Double avgMemberStress;
    private Double avgMemberDepression;
    private Double avgMemberAnxiety;
    private Double avgMemberResilience;
    private Double averageResilienceScore;

    // Performance
    private Double avgImprovementRate;
    private Integer successfulTransitions;
    private Double avgTimeInClusterDays;
    private Double retentionRate;

    // Interventions
    private String recommendedInterventions;
    private String primaryInterventionType;
    private Double avgInterventionEffectiveness;

    // Community
    private Boolean hasPeerSupport;
    private Double communityEngagementScore;
    private String sharedExperiences;
    private String copingStrategies;
    private String peerSupportActivities;
    private String professionalSupportLevel;

    // Quality Metrics
    private Double silhouetteScore;
    private Double cohesionScore;
    private Double separationScore;
    private Double clusterHealthScore;

    // Status
    private Boolean isActive;
    private String modelVersion;
    private LocalDateTime lastModelUpdate;
    private LocalDateTime lastUpdated;

    // Flags
    private Boolean needsAttention;
    private Boolean isAtCapacity;

    /**
     * Convert from Entity
     */
    public static ClusterGroupDTO fromEntity(ClusterGroup entity) {
        if (entity == null) return null;

        return ClusterGroupDTO.builder()
                .id(entity.getId())
                .clusterIdentifier(entity.getClusterIdentifier())
                .clusterName(entity.getClusterName())
                .description(entity.getDescription())
                .category(entity.getCategory() != null ? entity.getCategory().name() : null)
                .primaryCategoryDisplayName(entity.getCategory() != null
                        ? entity.getCategory().getDisplayName() : null)
                .level(entity.getLevel() != null ? entity.getLevel().name() : null)
                .severityLevelDisplayName(entity.getLevel() != null
                        ? entity.getLevel().getDisplayName() : null)
                .clusterIndex(entity.getClusterIndex())
                .centroidStress(entity.getCentroidStress())
                .centroidDepression(entity.getCentroidDepression())
                .centroidAnxiety(entity.getCentroidAnxiety())
                .centroidResilience(entity.getCentroidResilience())
                .memberCount(entity.getMemberCount())
                .activeMemberCount(entity.getActiveMemberCount())
                .maxCapacity(entity.getMaxCapacity())
                .avgMemberStress(entity.getAvgMemberStress())
                .avgMemberDepression(entity.getAvgMemberDepression())
                .avgMemberAnxiety(entity.getAvgMemberAnxiety())
                .avgMemberResilience(entity.getAvgMemberResilience())
                .averageResilienceScore(entity.getAverageResilienceScore())
                .avgImprovementRate(entity.getAvgImprovementRate())
                .successfulTransitions(entity.getSuccessfulTransitions())
                .avgTimeInClusterDays(entity.getAvgTimeInClusterDays())
                .retentionRate(entity.getRetentionRate())
                .recommendedInterventions(entity.getRecommendedInterventions())
                .primaryInterventionType(entity.getPrimaryInterventionType())
                .avgInterventionEffectiveness(entity.getAvgInterventionEffectiveness())
                .hasPeerSupport(entity.getHasPeerSupport())
                .communityEngagementScore(entity.getCommunityEngagementScore())
                .sharedExperiences(entity.getSharedExperiences())
                .copingStrategies(entity.getCopingStrategies())
                .peerSupportActivities(entity.getPeerSupportActivities())
                .professionalSupportLevel(entity.getProfessionalSupportLevel() != null ? entity.getProfessionalSupportLevel().name() : null)
                .silhouetteScore(entity.getSilhouetteScore())
                .cohesionScore(entity.getCohesionScore())
                .separationScore(entity.getSeparationScore())
                .clusterHealthScore(entity.getClusterHealthScore())
                .isActive(entity.getIsActive())
                .modelVersion(entity.getModelVersion())
                .lastModelUpdate(entity.getLastModelUpdate())
                .lastUpdated(entity.getLastUpdated())
                .needsAttention(entity.needsAttention())
                .isAtCapacity(entity.isAtCapacity())
                .build();
    }

    public void setCovarianceMatrix(String covarianceMatrix) {
    }

    public void setClusterWeight(Double clusterWeight) {
    }

    public ClusterCategory getPrimaryCategory() {
        return null;
    }

    public SeverityLevel getSeverityLevel() {
        return null;
    }
}