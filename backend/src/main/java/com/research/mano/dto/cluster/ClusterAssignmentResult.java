package com.research.mano.dto.cluster;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Result DTO for cluster assignment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterAssignmentResult {

    // Assigned Cluster
    private Long clusterId;
    private String clusterIdentifier;
    private String clusterName;
    private String primaryCategory;
    private String severityLevel;

    // Assignment Metrics
    private Double assignmentConfidence;
    private Double distanceToCentroid;
    private Double membershipProbability;

    // Alternative Clusters
    private List<AlternativeCluster> alternativeClusters;

    // Transition Info (if applicable)
    private Boolean isTransition;
    private Long transitionId;
    private String previousClusterIdentifier;
    private String transitionType;
    private String transitionDirection;

    // Recommendations
    private List<String> recommendedInterventions;
    private List<String> copingStrategies;
    private Boolean peerSupportAvailable;

    // Risk Assessment
    private Boolean requiresImmediateAttention;
    private List<String> riskIndicators;

    // Metadata
    private String modelVersion;
    private Boolean usedFallback;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlternativeCluster {
        private Long clusterId;
        private String clusterIdentifier;
        private String clusterName;
        private Double distance;
        private Double probability;
    }

    /**
     * Create from ML service response
     */
    public static ClusterAssignmentResult fromMLResponse(Map<String, Object> mlResponse, ClusterGroupDTO cluster) {
        ClusterAssignmentResultBuilder builder = ClusterAssignmentResult.builder()
                .clusterIdentifier((String) mlResponse.get("clusterIdentifier"))
                .assignmentConfidence((Double) mlResponse.getOrDefault("confidence", 0.8))
                .modelVersion((String) mlResponse.get("modelVersion"))
                .usedFallback((Boolean) mlResponse.getOrDefault("isFallback", false));

        if (cluster != null) {
            builder.clusterId(cluster.getId())
                    .clusterName(cluster.getClusterName())
                    .primaryCategory(cluster.getPrimaryCategory() != null
                            ? cluster.getPrimaryCategory().name() : null)
                    .severityLevel(cluster.getSeverityLevel() != null
                            ? cluster.getSeverityLevel().name() : null)
                    .peerSupportAvailable(cluster.getHasPeerSupport())
                    .copingStrategies(cluster.getCopingStrategies() != null
                            ? List.of(cluster.getCopingStrategies().split(",")) : null);
        }

        return builder.build();
    }
}