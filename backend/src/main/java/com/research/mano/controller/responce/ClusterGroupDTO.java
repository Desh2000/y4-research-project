package com.research.mano.controller.responce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClusterGroupDTO {
    private Long id;
    private String clusterIdentifier;
    private String category;
    private String level;
    private String description;
    private Integer memberCount;
    private Double averageResilienceScore;
    private String professionalSupportLevel;
    private String recommendedInterventions;
    private String peerSupportActivities;
    private LocalDateTime lastUpdated;
    private String modelVersion;

    // GMM parameters
    private Double centroidStress;
    private Double centroidDepression;
    private Double centroidAnxiety;
    private String covarianceMatrix;
    private Double clusterWeight;
}