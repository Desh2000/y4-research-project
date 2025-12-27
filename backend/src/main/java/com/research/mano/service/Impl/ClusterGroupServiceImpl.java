package com.research.mano.service.Impl;

import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.repository.ClusterGroupRepository;
import com.research.mano.repository.UserProfileRepository;
import com.research.mano.service.ClusterGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cluster Group Service Implementation
 * Handles Component 4 (GMM Clustering System) business logic
 */
@Service
@Transactional
public class ClusterGroupServiceImpl implements ClusterGroupService {

    @Autowired
    private ClusterGroupRepository clusterGroupRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    public ClusterGroup save(ClusterGroup clusterGroup) {
        return clusterGroupRepository.save(clusterGroup);
    }

    @Override
    public List<ClusterGroup> saveAll(List<ClusterGroup> clusterGroups) {
        return clusterGroupRepository.saveAll(clusterGroups);
    }

    @Override
    public Optional<ClusterGroup> findById(Long id) {
        return clusterGroupRepository.findById(id);
    }

    @Override
    public List<ClusterGroup> findAll() {
        return clusterGroupRepository.findAll();
    }

    @Override
    public Page<ClusterGroup> findAll(Pageable pageable) {
        return clusterGroupRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return clusterGroupRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        clusterGroupRepository.deleteById(id);
    }

    @Override
    public void delete(ClusterGroup clusterGroup) {
        clusterGroupRepository.delete(clusterGroup);
    }

    @Override
    public long count() {
        return clusterGroupRepository.count();
    }

    @Override
    public void initializeAllClusters() {
        if (areAllClustersInitialized()) {
            return; // Already initialized
        }

        for (MentalHealthPrediction.ClusterCategory category : MentalHealthPrediction.ClusterCategory.values()) {
            for (MentalHealthPrediction.ClusterLevel level : MentalHealthPrediction.ClusterLevel.values()) {
                String clusterIdentifier = category.name() + "_" + level.name();

                if (!clusterGroupRepository.findByClusterIdentifier(clusterIdentifier).isPresent()) {
                    ClusterGroup cluster = new ClusterGroup(category, level);
                    cluster.setModelVersion("1.0.0");
                    clusterGroupRepository.save(cluster);
                }
            }
        }
    }

    @Override
    public Optional<ClusterGroup> findByClusterIdentifier(String clusterIdentifier) {
        return clusterGroupRepository.findByClusterIdentifier(clusterIdentifier);
    }

    @Override
    public Optional<ClusterGroup> findByCategoryAndLevel(MentalHealthPrediction.ClusterCategory category,
                                                         MentalHealthPrediction.ClusterLevel level) {
        return clusterGroupRepository.findByCategoryAndLevel(category, level);
    }

    @Override
    public List<ClusterGroup> getClustersByCategory(MentalHealthPrediction.ClusterCategory category) {
        return clusterGroupRepository.findByCategory(category);
    }

    @Override
    public List<ClusterGroup> getClustersByLevel(MentalHealthPrediction.ClusterLevel level) {
        return clusterGroupRepository.findByLevel(level);
    }

    @Override
    public Optional<ClusterGroup> findClusterForPrediction(Double stressScore, Double depressionScore, Double anxietyScore) {
        // Find the dominant category (highest score)
        MentalHealthPrediction.ClusterCategory category;
        Double maxScore = Math.max(stressScore, Math.max(depressionScore, anxietyScore));

        if (maxScore.equals(stressScore)) {
            category = MentalHealthPrediction.ClusterCategory.STRESS;
        } else if (maxScore.equals(depressionScore)) {
            category = MentalHealthPrediction.ClusterCategory.DEPRESSION;
        } else {
            category = MentalHealthPrediction.ClusterCategory.ANXIETY;
        }

        // Determine level based on dominant score
        MentalHealthPrediction.ClusterLevel level;
        if (maxScore >= 0.8) {
            level = MentalHealthPrediction.ClusterLevel.HIGH;
        } else if (maxScore >= 0.4) {
            level = MentalHealthPrediction.ClusterLevel.MEDIUM;
        } else {
            level = MentalHealthPrediction.ClusterLevel.LOW;
        }

        return findByCategoryAndLevel(category, level);
    }

    @Override
    public ClusterGroup updateClusterCentroid(Long clusterId, Double stressCentroid,
                                              Double depressionCentroid, Double anxietyCentroid,
                                              String modelVersion) {
        clusterGroupRepository.updateCentroid(clusterId, stressCentroid, depressionCentroid,
                anxietyCentroid, LocalDateTime.now(), modelVersion);
        return clusterGroupRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));
    }

    @Override
    public ClusterGroup updateMemberCount(Long clusterId, Integer memberCount) {
        clusterGroupRepository.updateMemberCount(clusterId, memberCount, LocalDateTime.now());
        return clusterGroupRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));
    }

    @Override
    public void incrementMemberCount(Long clusterId) {
        clusterGroupRepository.incrementMemberCount(clusterId, LocalDateTime.now());
    }

    @Override
    public void decrementMemberCount(Long clusterId) {
        clusterGroupRepository.decrementMemberCount(clusterId, LocalDateTime.now());
    }

    @Override
    public ClusterGroup updateAverageResilienceScore(Long clusterId, Double avgScore) {
        clusterGroupRepository.updateAverageResilienceScore(clusterId, avgScore, LocalDateTime.now());
        return clusterGroupRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));
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
    public List<ClusterGroup> getMostPopulatedClusters() {
        return clusterGroupRepository.findMostPopulatedClusters();
    }

    @Override
    public List<ClusterGroup> getHighestResilienceClusters() {
        return clusterGroupRepository.findHighestResilienceClusters();
    }

    @Override
    public List<ClusterGroup> getLowestResilienceClusters() {
        return clusterGroupRepository.findLowestResilienceClusters();
    }

    @Override
    public List<ClusterGroup> getNonEmptyClusters() {
        return clusterGroupRepository.findNonEmptyClusterGroups();
    }

    @Override
    public List<ClusterGroup> getEmptyClusters() {
        return clusterGroupRepository.findEmptyClusterGroups();
    }

    @Override
    public List<ClusterGroup> findClustersNeedingUpdate(int daysOld, String currentModelVersion) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return clusterGroupRepository.findClusterGroupsNeedingUpdate(cutoffDate, currentModelVersion);
    }

    @Override
    public void assignUserToCluster(Long userId, String clusterIdentifier) {
        ClusterGroup cluster = clusterGroupRepository.findByClusterIdentifier(clusterIdentifier)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterIdentifier));

        userProfileRepository.updateClusterAssignment(userId, cluster, LocalDateTime.now());
        incrementMemberCount(cluster.getId());
    }

    @Override
    public void moveUserBetweenClusters(Long userId, String fromClusterIdentifier, String toClusterIdentifier) {
        // Decrement from the old cluster
        if (fromClusterIdentifier != null) {
            ClusterGroup fromCluster = clusterGroupRepository.findByClusterIdentifier(fromClusterIdentifier)
                    .orElse(null);
            if (fromCluster != null) {
                decrementMemberCount(fromCluster.getId());
            }
        }

        // Assign to a new cluster
        assignUserToCluster(userId, toClusterIdentifier);
    }

    @Override
    public Long getTotalMemberCount() {
        return clusterGroupRepository.getTotalMemberCount();
    }

    @Override
    public boolean areAllClustersInitialized() {
        Long activeClusters = clusterGroupRepository.countActiveClusters();
        return activeClusters >= 9; // 3 categories × 3 levels = 9 clusters
    }

    @Override
    public List<String> getMissingClusterIdentifiers() {
        return clusterGroupRepository.findMissingClusterIdentifiers();
    }

    @Override
    public ClusterGroup updateGMMParameters(Long clusterId, String covarianceMatrix, Double clusterWeight) {
        ClusterGroup cluster = clusterGroupRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));

        cluster.setCovarianceMatrix(covarianceMatrix);
        cluster.setClusterWeight(clusterWeight);
        cluster.setLastUpdated(LocalDateTime.now());

        return clusterGroupRepository.save(cluster);
    }

    @Override
    public void recalculateClusterStatistics(Long clusterId) {
        ClusterGroup cluster = clusterGroupRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));

        // Get all profiles in this cluster and recalculate stats
        List<Object[]> clusterStats = userProfileRepository.getAverageScoresByCluster();

        for (Object[] stats : clusterStats) {
            String clusterIdFromStats = (String) stats[0];
            if (cluster.getClusterIdentifier().equals(clusterIdFromStats)) {
                Double avgStress = (Double) stats[1];
                Double avgAnxiety = (Double) stats[2];
                Double avgDepression = (Double) stats[3];

                cluster.setCentroidStress(avgStress);
                cluster.setCentroidAnxiety(avgAnxiety);
                cluster.setCentroidDepression(avgDepression);

                break;
            }
        }

        cluster.setLastUpdated(LocalDateTime.now());
        clusterGroupRepository.save(cluster);
    }

    @Override
    public List<ClusterGroup> findByProfessionalSupportLevel(ClusterGroup.ProfessionalSupportLevel supportLevel) {
        return clusterGroupRepository.findByProfessionalSupportLevel(supportLevel);
    }

    @Override
    public List<ClusterGroup> getHighIntensitySupportClusters() {
        return clusterGroupRepository.findHighIntensitySupportClusters();
    }

    @Override
    public List<ClusterGroup> getLowIntensitySupportClusters() {
        return clusterGroupRepository.findLowIntensitySupportClusters();
    }

    @Override
    public ClusterGroup updateClusterRecommendations(Long clusterId, String interventions, String peerActivities) {
        ClusterGroup cluster = clusterGroupRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found"));

        cluster.setRecommendedInterventions(interventions);
        cluster.setPeerSupportActivities(peerActivities);
        cluster.setLastUpdated(LocalDateTime.now());

        return clusterGroupRepository.save(cluster);
    }

    @Override
    public boolean validateClusterIntegrity() {
        Long activeCount = clusterGroupRepository.countActiveClusters();
        return activeCount == 9; // Exactly 9 clusters should exist
    }
}