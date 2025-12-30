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
    private ClusterCategory primaryCategory;
    private String primaryCategoryDisplayName;
    private SeverityLevel severityLevel;
    private String severityLevelDisplayName;
    private Integer clusterIndex;

    // Centroids
    private Double centroidStress;
    private Double centroidDepression;
    private Double centroidAnxiety;
    private Double centroidResilience;

    // Member Statistics
    private Integer memberCount;
    private Integer activeMemberCount;
    private Integer maxCapacity;
    private Double avgMemberStress;
    private Double avgMemberDepression;
    private Double avgMemberAnxiety;
    private Double avgMemberResilience;

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

    // Quality Metrics
    private Double silhouetteScore;
    private Double cohesionScore;
    private Double separationScore;
    private Double clusterHealthScore;

    // Status
    private Boolean isActive;
    private String modelVersion;
    private LocalDateTime lastModelUpdate;

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
                .primaryCategory(entity.getPrimaryCategory())
                .primaryCategoryDisplayName(entity.getPrimaryCategory() != null
                        ? entity.getPrimaryCategory().getDisplayName() : null)
                .severityLevel(entity.getSeverityLevel())
                .severityLevelDisplayName(entity.getSeverityLevel() != null
                        ? entity.getSeverityLevel().getDisplayName() : null)
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
                .silhouetteScore(entity.getSilhouetteScore())
                .cohesionScore(entity.getCohesionScore())
                .separationScore(entity.getSeparationScore())
                .clusterHealthScore(entity.getClusterHealthScore())
                .isActive(entity.getIsActive())
                .modelVersion(entity.getModelVersion())
                .lastModelUpdate(entity.getLastModelUpdate())
                .needsAttention(entity.needsAttention())
                .isAtCapacity(entity.isAtCapacity())
                .build();
    }
}