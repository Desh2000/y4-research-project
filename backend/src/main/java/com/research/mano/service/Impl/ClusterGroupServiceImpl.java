package com.research.mano.service.Impl;

import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.repository.ClusterGroupRepository;
import com.research.mano.service.ClusterGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClusterGroupServiceImpl implements ClusterGroupService {

    @Autowired
    private ClusterGroupRepository clusterGroupRepository;

    @Override
    public List<ClusterGroup> findAll() {
        return clusterGroupRepository.findAll();
    }

    @Override
    public Optional<ClusterGroup> findById(Long id) {
        return clusterGroupRepository.findById(id);
    }

    @Override
    public Optional<ClusterGroup> findByClusterIdentifier(String clusterIdentifier) {
        return clusterGroupRepository.findByClusterIdentifier(clusterIdentifier);
    }

    @Override
    public List<ClusterGroup> getClustersByCategory(MentalHealthPrediction.ClusterCategory category) {
        // Map MentalHealthPrediction.ClusterCategory to ClusterGroup.ClusterCategory
        ClusterGroup.ClusterCategory mappedCategory = ClusterGroup.ClusterCategory.valueOf(category.name());
        return clusterGroupRepository.findByCategory(mappedCategory);
    }

    @Override
    public List<ClusterGroup> getClustersByLevel(MentalHealthPrediction.ClusterLevel level) {
        // Map MentalHealthPrediction.ClusterLevel to ClusterGroup.SeverityLevel
        ClusterGroup.SeverityLevel mappedLevel = ClusterGroup.SeverityLevel.valueOf(level.name());
        return clusterGroupRepository.findByLevel(mappedLevel);
    }

    @Override
    public Optional<ClusterGroup> findClusterForPrediction(Double stressScore, Double depressionScore, Double anxietyScore) {
        // This logic should ideally use the ML model or distance calculation
        // For now, using a simple heuristic based on dominant score and level
        
        ClusterGroup.ClusterCategory category;
        double maxScore = Math.max(stressScore, Math.max(depressionScore, anxietyScore));

        if (maxScore == stressScore) {
            category = ClusterGroup.ClusterCategory.STRESS;
        } else if (maxScore == depressionScore) {
            category = ClusterGroup.ClusterCategory.DEPRESSION;
        } else {
            category = ClusterGroup.ClusterCategory.ANXIETY;
        }

        ClusterGroup.SeverityLevel level;
        if (maxScore >= 0.8) {
            level = ClusterGroup.SeverityLevel.SEVERE;
        } else if (maxScore >= 0.6) {
            level = ClusterGroup.SeverityLevel.HIGH;
        } else if (maxScore >= 0.4) {
            level = ClusterGroup.SeverityLevel.MODERATE;
        } else if (maxScore >= 0.2) {
            level = ClusterGroup.SeverityLevel.LOW;
        } else {
            level = ClusterGroup.SeverityLevel.MINIMAL;
        }

        return clusterGroupRepository.findByCategoryAndLevel(category, level);
    }

    @Override
    public List<ClusterGroup> getMostPopulatedClusters() {
        return clusterGroupRepository.findTop5ByOrderByMemberCountDesc();
    }

    @Override
    public List<ClusterGroup> getHighestResilienceClusters() {
        return clusterGroupRepository.findTop5ByOrderByAverageResilienceScoreDesc();
    }

    @Override
    public List<ClusterGroup> getLowestResilienceClusters() {
        return clusterGroupRepository.findTop5ByOrderByAverageResilienceScoreAsc();
    }

    @Override
    public List<ClusterGroup> findByProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel level) {
        return clusterGroupRepository.findByProfessionalSupportLevel(level);
    }

    @Override
    public List<Object[]> getClusterDistributionStats() {
        return clusterGroupRepository.getClusterDistributionStats();
    }

    @Override
    public List<Object[]> getStatsByCategory() {
        return clusterGroupRepository.getStatsByCategory();
    }

    @Override
    public List<Object[]> getStatsByLevel() {
        return clusterGroupRepository.getStatsByLevel();
    }

    @Override
    public void initializeAllClusters() {
        for (ClusterGroup.ClusterCategory category : ClusterGroup.ClusterCategory.values()) {
            for (ClusterGroup.SeverityLevel level : ClusterGroup.SeverityLevel.values()) {
                String identifier = category.name() + "_" + level.name();
                if (clusterGroupRepository.findByClusterIdentifier(identifier).isEmpty()) {
                    ClusterGroup cluster = new ClusterGroup();
                    cluster.setClusterIdentifier(identifier);
                    cluster.setCategory(category);
                    cluster.setLevel(level);
                    cluster.setClusterDescription(category.getDescription() + " - " + level.getDisplayName());
                    cluster.setMemberCount(0L);
                    cluster.setAverageResilienceScore(0.0);
                    cluster.setLastUpdated(LocalDateTime.now());
                    
                    // Set default support level based on severity
                    if (level == ClusterGroup.SeverityLevel.SEVERE || level == ClusterGroup.SeverityLevel.HIGH) {
                        cluster.setProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel.HIGH);
                    } else if (level == ClusterGroup.SeverityLevel.MODERATE) {
                        cluster.setProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel.MEDIUM);
                    } else {
                        cluster.setProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel.LOW);
                    }
                    
                    clusterGroupRepository.save(cluster);
                }
            }
        }
    }

    @Override
    public ClusterGroup updateClusterCentroid(Long id, Double stressCentroid, Double depressionCentroid, Double anxietyCentroid, String modelVersion) {
        ClusterGroup cluster = clusterGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + id));
        
        cluster.setCentroidStress(stressCentroid);
        cluster.setCentroidDepression(depressionCentroid);
        cluster.setCentroidAnxiety(anxietyCentroid);
        cluster.setModelVersion(modelVersion);
        cluster.setLastUpdated(LocalDateTime.now());
        
        return clusterGroupRepository.save(cluster);
    }

    @Override
    public ClusterGroup updateClusterRecommendations(Long id, String interventions, String peerActivities) {
        ClusterGroup cluster = clusterGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + id));
        
        cluster.setRecommendedInterventions(interventions);
        cluster.setPeerSupportActivities(peerActivities);
        cluster.setLastUpdated(LocalDateTime.now());
        
        return clusterGroupRepository.save(cluster);
    }

    @Override
    public void recalculateClusterStatistics(Long id) {
        // Logic to recalculate statistics based on members would go here
        // For now, just updating the timestamp
        ClusterGroup cluster = clusterGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + id));
        cluster.setLastUpdated(LocalDateTime.now());
        clusterGroupRepository.save(cluster);
    }

    @Override
    public boolean validateClusterIntegrity() {
        return getMissingClusterIdentifiers().isEmpty();
    }

    @Override
    public List<String> getMissingClusterIdentifiers() {
        List<String> missing = new ArrayList<>();
        for (ClusterGroup.ClusterCategory category : ClusterGroup.ClusterCategory.values()) {
            for (ClusterGroup.SeverityLevel level : ClusterGroup.SeverityLevel.values()) {
                String identifier = category.name() + "_" + level.name();
                if (clusterGroupRepository.findByClusterIdentifier(identifier).isEmpty()) {
                    missing.add(identifier);
                }
            }
        }
        return missing;
    }

    @Override
    public Long getTotalMemberCount() {
        return clusterGroupRepository.sumMemberCount();
    }
}