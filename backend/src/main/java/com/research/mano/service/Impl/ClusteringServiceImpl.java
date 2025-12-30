package com.research.mano.service.Impl;

import com.research.mano.dto.cluster.*;
import com.research.mano.entity.ClusterGroup;
import com.research.mano.entity.ClusterGroup.ClusterCategory;
import com.research.mano.entity.ClusterGroup.SeverityLevel;
import com.research.mano.entity.ClusterTransition;
import com.research.mano.entity.ClusterTransition.TriggerType;
import com.research.mano.entity.User;
import com.research.mano.entity.UserProfile;
import com.research.mano.repository.ClusterGroupRepository;
import com.research.mano.repository.ClusterTransitionRepository;
import com.research.mano.repository.UserRepository;
import com.research.mano.repository.UserProfileRepository;
import com.research.mano.service.ClusteringService;
import com.research.mano.service.ml.MLServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clustering Service Implementation for Component 4
 * GMM-based clustering with community support features
 */
@Service
@Transactional
public class ClusteringServiceImpl implements ClusteringService {

    private static final Logger logger = LoggerFactory.getLogger(ClusteringServiceImpl.class);

    @Autowired
    private ClusterGroupRepository clusterRepository;

    @Autowired
    private ClusterTransitionRepository transitionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private MLServiceClient mlServiceClient;

    // ==================== CLUSTER ASSIGNMENT ====================

    @Override
    public ClusterAssignmentResult assignUserToCluster(Long userId, ClusterAssignmentRequest request) {
        User user = findUserById(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));

        // Get previous cluster
        ClusterGroup previousCluster = profile.getCurrentClusterGroup();

        // Find appropriate cluster
        ClusterAssignmentResult result = performClusterAssignment(
                request.getStressScore(),
                request.getDepressionScore(),
                request.getAnxietyScore()
        );

        // Get or create the cluster
        ClusterGroup newCluster = clusterRepository.findByClusterIdentifier(result.getClusterIdentifier())
                .orElseGet(() -> createClusterFromResult(result));

        // Check if transition occurred
        boolean isTransition = previousCluster == null ||
                !previousCluster.getId().equals(newCluster.getId());

        if (isTransition) {
            // Create transition record
            ClusterTransition transition = new ClusterTransition(user, previousCluster, newCluster,
                    request.getStressScore(), request.getDepressionScore(), request.getAnxietyScore());

            transition.setAssignmentConfidence(result.getAssignmentConfidence());
            transition.setDistanceToNewCentroid(result.getDistanceToCentroid());
            transition.setModelVersion(result.getModelVersion());

            // Set trigger
            if (request.getPredictionId() != null) {
                transition.setTriggerFromPrediction(request.getPredictionId());
            } else if (request.getInterventionId() != null) {
                transition.setTriggerFromIntervention(request.getInterventionId());
            } else {
                transition.setTriggerType(TriggerType.PERIODIC_REASSESSMENT);
            }
            transition.setTriggerDescription(request.getTriggerDescription());

            // Calculate distance to old centroid
            if (previousCluster != null) {
                transition.setDistanceToOldCentroid(previousCluster.calculateDistanceFromCentroid(
                        request.getStressScore(), request.getDepressionScore(), request.getAnxietyScore()));
                transition.setPreviousClusterEntryDate(profile.getClusterAssignmentDate());
            }

            transitionRepository.save(transition);

            // Update cluster member counts
            if (previousCluster != null) {
                previousCluster.removeMember();
                clusterRepository.save(previousCluster);
            }
            newCluster.addMember();
            clusterRepository.save(newCluster);

            // Update result with transition info
            result.setIsTransition(true);
            result.setTransitionId(transition.getId());
            result.setPreviousClusterIdentifier(previousCluster != null ?
                    previousCluster.getClusterIdentifier() : null);
            result.setTransitionType(transition.getTransitionType().name());
            result.setTransitionDirection(transition.getTransitionDirection().name());

            // Record successful transition for previous cluster if improvement
            if (previousCluster != null && transition.isPositiveTransition()) {
                previousCluster.recordSuccessfulTransition();
                clusterRepository.save(previousCluster);
            }
        } else {
            result.setIsTransition(false);
        }

        // Update user profile
        profile.setCurrentClusterGroup(newCluster);
        profile.setClusterAssignmentDate(LocalDateTime.now());
        profile.setCurrentStressLevel(request.getStressScore());
        profile.setCurrentAnxietyLevel(request.getAnxietyScore());
        profile.setCurrentDepressionLevel(request.getDepressionScore());
        userProfileRepository.save(profile);

        // Add recommendations
        result.setRecommendedInterventions(parseJsonArray(newCluster.getRecommendedInterventions()));
        result.setCopingStrategies(parseJsonArray(newCluster.getCopingStrategies()));
        result.setPeerSupportAvailable(newCluster.getHasPeerSupport());

        // Check if attention needed
        result.setRequiresImmediateAttention(newCluster.needsAttention() ||
                newCluster.getSeverityLevel() == SeverityLevel.SEVERE);

        return result;
    }

    @Override
    public ClusterAssignmentResult previewClusterAssignment(Double stressScore, Double depressionScore,
                                                            Double anxietyScore) {
        return performClusterAssignment(stressScore, depressionScore, anxietyScore);
    }

    @Override
    public ClusterAssignmentResult reassessUserCluster(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found: " + userId));

        ClusterAssignmentRequest request = new ClusterAssignmentRequest();
        request.setStressScore(profile.getCurrentStressLevel() != null ? profile.getCurrentStressLevel() : 0.5);
        request.setDepressionScore(profile.getCurrentDepressionLevel() != null ? profile.getCurrentDepressionLevel() : 0.5);
        request.setAnxietyScore(profile.getCurrentAnxietyLevel() != null ? profile.getCurrentAnxietyLevel() : 0.5);
        request.setTriggerDescription("Periodic reassessment");

        return assignUserToCluster(userId, request);
    }

    @Override
    public Optional<ClusterGroupDTO> getUserCurrentCluster(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfile::getCurrentClusterGroup)
                .map(ClusterGroupDTO::fromEntity);
    }

    @Override
    public ClusterGroupDTO findNearestCluster(Double stressScore, Double depressionScore, Double anxietyScore) {
        return clusterRepository.findNearestCluster(stressScore, depressionScore, anxietyScore)
                .map(ClusterGroupDTO::fromEntity)
                .orElseGet(() -> {
                    // Fallback: determine cluster manually
                    ClusterCategory category = determineCategory(stressScore, depressionScore, anxietyScore);
                    SeverityLevel severity = determineSeverity(stressScore, depressionScore, anxietyScore);
                    String identifier = category.name() + "_" + severity.name();

                    return clusterRepository.findByClusterIdentifier(identifier)
                            .map(ClusterGroupDTO::fromEntity)
                            .orElse(null);
                });
    }

    @Override
    public List<ClusterAssignmentResult.AlternativeCluster> findNearestClusters(
            Double stressScore, Double depressionScore, Double anxietyScore, int topN) {

        List<ClusterGroup> allClusters = clusterRepository.findAllActiveOrdered();

        return allClusters.stream()
                .map(cluster -> {
                    double distance = cluster.calculateDistanceFromCentroid(stressScore, depressionScore, anxietyScore);
                    return ClusterAssignmentResult.AlternativeCluster.builder()
                            .clusterId(cluster.getId())
                            .clusterIdentifier(cluster.getClusterIdentifier())
                            .clusterName(cluster.getClusterName())
                            .distance(distance)
                            .probability(1.0 / (1.0 + distance)) // Simple inverse distance probability
                            .build();
                })
                .sorted(Comparator.comparingDouble(ClusterAssignmentResult.AlternativeCluster::getDistance))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // ==================== CLUSTER MANAGEMENT ====================

    @Override
    public List<ClusterGroupDTO> getAllActiveClusters() {
        return clusterRepository.findAllActiveOrdered().stream()
                .map(ClusterGroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClusterGroupDTO> getClusterById(Long clusterId) {
        return clusterRepository.findById(clusterId)
                .map(ClusterGroupDTO::fromEntity);
    }

    @Override
    public Optional<ClusterGroupDTO> getClusterByIdentifier(String identifier) {
        return clusterRepository.findByClusterIdentifier(identifier)
                .map(ClusterGroupDTO::fromEntity);
    }

    @Override
    public List<ClusterGroupDTO> getClustersByCategory(ClusterCategory category) {
        return clusterRepository.findActiveByCategoryOrdered(category).stream()
                .map(ClusterGroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterGroupDTO> getClustersBySeverity(SeverityLevel severityLevel) {
        return clusterRepository.findBySeverityLevel(severityLevel).stream()
                .filter(ClusterGroup::getIsActive)
                .map(ClusterGroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ClusterGroupDTO createCluster(ClusterGroupDTO dto) {
        ClusterGroup cluster = new ClusterGroup();
        updateClusterFromDTO(cluster, dto);
        cluster.setIsActive(true);

        ClusterGroup saved = clusterRepository.save(cluster);
        return ClusterGroupDTO.fromEntity(saved);
    }

    @Override
    public ClusterGroupDTO updateCluster(Long clusterId, ClusterGroupDTO dto) {
        ClusterGroup cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));

        updateClusterFromDTO(cluster, dto);

        ClusterGroup saved = clusterRepository.save(cluster);
        return ClusterGroupDTO.fromEntity(saved);
    }

    @Override
    public void deactivateCluster(Long clusterId) {
        ClusterGroup cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));

        cluster.setIsActive(false);
        clusterRepository.save(cluster);
    }

    @Override
    public ClusterGroupDTO updateClusterCentroids(Long clusterId, Double stressCentroid,
                                                  Double depressionCentroid, Double anxietyCentroid) {
        ClusterGroup cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));

        cluster.setCentroidStress(stressCentroid);
        cluster.setCentroidDepression(depressionCentroid);
        cluster.setCentroidAnxiety(anxietyCentroid);
        cluster.setLastModelUpdate(LocalDateTime.now());

        ClusterGroup saved = clusterRepository.save(cluster);
        return ClusterGroupDTO.fromEntity(saved);
    }

    // ==================== TRANSITION MANAGEMENT ====================

    @Override
    public List<ClusterTransitionDTO> getUserTransitionHistory(Long userId) {
        return transitionRepository.findByUserIdOrdered(userId).stream()
                .map(ClusterTransitionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterTransitionDTO> getUserTransitionHistory(Long userId, int page, int size) {
        User user = findUserById(userId);
        return transitionRepository.findByUserOrderByTransitionDateDesc(user, PageRequest.of(page, size))
                .stream()
                .map(ClusterTransitionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ClusterTransitionDTO> getUserLatestTransition(Long userId) {
        return transitionRepository.findLatestByUserId(userId)
                .map(ClusterTransitionDTO::fromEntity);
    }

    @Override
    public List<ClusterTransitionDTO> getClusterTransitions(Long clusterId) {
        return transitionRepository.findByClusterId(clusterId).stream()
                .map(ClusterTransitionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterTransitionDTO> getRecentTransitions(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return transitionRepository.findRecentTransitions(since).stream()
                .map(ClusterTransitionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterTransitionDTO> getImprovements(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return transitionRepository.findByDateRange(since, LocalDateTime.now()).stream()
                .filter(t -> t.isPositiveTransition())
                .map(ClusterTransitionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterTransitionDTO> getDeteriorations(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return transitionRepository.findByDateRange(since, LocalDateTime.now()).stream()
                .filter(t -> t.isNegativeTransition())
                .map(ClusterTransitionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ClusterTransitionDTO trackTransitionOutcome(Long transitionId, boolean successful, String notes) {
        ClusterTransition transition = transitionRepository.findById(transitionId)
                .orElseThrow(() -> new RuntimeException("Transition not found: " + transitionId));

        transition.trackOutcome(successful, notes);

        ClusterTransition saved = transitionRepository.save(transition);
        return ClusterTransitionDTO.fromEntity(saved);
    }

    // ==================== ANALYTICS ====================

    @Override
    public Map<String, Object> getClusterStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalClusters", clusterRepository.count());
        stats.put("activeClusters", clusterRepository.findByIsActiveTrue().size());
        stats.put("totalMembers", clusterRepository.getTotalMemberCount());
        stats.put("activeMembers", clusterRepository.getTotalActiveMemberCount());

        // By category
        List<Object[]> byCategory = clusterRepository.getStatisticsByCategory();
        Map<String, Map<String, Object>> categoryStats = new HashMap<>();
        for (Object[] row : byCategory) {
            Map<String, Object> catStat = new HashMap<>();
            catStat.put("clusterCount", row[1]);
            catStat.put("memberCount", row[2]);
            categoryStats.put(row[0].toString(), catStat);
        }
        stats.put("byCategory", categoryStats);

        // By severity
        List<Object[]> bySeverity = clusterRepository.getStatisticsBySeverity();
        Map<String, Map<String, Object>> severityStats = new HashMap<>();
        for (Object[] row : bySeverity) {
            Map<String, Object> sevStat = new HashMap<>();
            sevStat.put("clusterCount", row[1]);
            sevStat.put("memberCount", row[2]);
            severityStats.put(row[0].toString(), sevStat);
        }
        stats.put("bySeverity", severityStats);

        // Performance metrics
        Object[] perfMetrics = clusterRepository.getOverallPerformanceMetrics();
        if (perfMetrics != null && perfMetrics.length >= 3) {
            stats.put("avgImprovementRate", perfMetrics[0]);
            stats.put("avgRetentionRate", perfMetrics[1]);
            stats.put("avgSilhouetteScore", perfMetrics[2]);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getClusterDetailedStatistics(Long clusterId) {
        ClusterGroup cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("cluster", ClusterGroupDTO.fromEntity(cluster));

        // Member statistics
        stats.put("memberCount", cluster.getMemberCount());
        stats.put("activeMemberCount", cluster.getActiveMemberCount());
        stats.put("avgMemberStress", cluster.getAvgMemberStress());
        stats.put("avgMemberDepression", cluster.getAvgMemberDepression());
        stats.put("avgMemberAnxiety", cluster.getAvgMemberAnxiety());

        // Transition statistics
        List<ClusterTransition> incomingTransitions = transitionRepository.findByToCluster(cluster);
        List<ClusterTransition> outgoingTransitions = transitionRepository.findByFromCluster(cluster);

        stats.put("totalIncomingTransitions", incomingTransitions.size());
        stats.put("totalOutgoingTransitions", outgoingTransitions.size());
        stats.put("improvementTransitions", outgoingTransitions.stream()
                .filter(ClusterTransition::isPositiveTransition).count());
        stats.put("deteriorationTransitions", outgoingTransitions.stream()
                .filter(ClusterTransition::isNegativeTransition).count());

        // Performance
        stats.put("avgImprovementRate", cluster.getAvgImprovementRate());
        stats.put("retentionRate", cluster.getRetentionRate());
        stats.put("clusterHealthScore", cluster.getClusterHealthScore());

        return stats;
    }

    @Override
    public Map<String, Object> getTransitionMatrix(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> matrix = transitionRepository.getTransitionMatrix(since);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> transitions = new ArrayList<>();

        for (Object[] row : matrix) {
            Map<String, Object> transition = new HashMap<>();
            transition.put("from", row[0]);
            transition.put("to", row[1]);
            transition.put("count", row[2]);
            transitions.add(transition);
        }

        result.put("transitions", transitions);
        result.put("periodDays", days);
        result.put("totalTransitions", transitionRepository.countTransitionsSince(since));

        return result;
    }

    @Override
    public Map<String, Object> getCategoryTransitionMatrix(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> matrix = transitionRepository.getCategoryTransitionMatrix(since);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> transitions = new ArrayList<>();

        for (Object[] row : matrix) {
            Map<String, Object> transition = new HashMap<>();
            transition.put("fromCategory", row[0]);
            transition.put("toCategory", row[1]);
            transition.put("count", row[2]);
            transitions.add(transition);
        }

        result.put("transitions", transitions);
        return result;
    }

    @Override
    public Map<String, Object> getSeverityTransitionMatrix(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> matrix = transitionRepository.getSeverityTransitionMatrix(since);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> transitions = new ArrayList<>();

        for (Object[] row : matrix) {
            Map<String, Object> transition = new HashMap<>();
            transition.put("fromSeverity", row[0]);
            transition.put("toSeverity", row[1]);
            transition.put("count", row[2]);
            transitions.add(transition);
        }

        result.put("transitions", transitions);
        return result;
    }

    @Override
    public Map<String, Object> getUserJourneyAnalysis(Long userId) {
        List<ClusterTransition> transitions = transitionRepository.findByUserIdOrdered(userId);

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalTransitions", transitions.size());

        if (!transitions.isEmpty()) {
            // First and current cluster
            ClusterTransition first = transitions.get(transitions.size() - 1);
            ClusterTransition latest = transitions.get(0);

            analysis.put("initialCluster", first.getToCluster() != null ?
                    first.getToCluster().getClusterIdentifier() : null);
            analysis.put("currentCluster", latest.getToCluster() != null ?
                    latest.getToCluster().getClusterIdentifier() : null);

            // Count improvements and deteriorations
            long improvements = transitions.stream().filter(ClusterTransition::isPositiveTransition).count();
            long deteriorations = transitions.stream().filter(ClusterTransition::isNegativeTransition).count();

            analysis.put("improvements", improvements);
            analysis.put("deteriorations", deteriorations);

            // Overall trajectory
            if (first.getToCluster() != null && latest.getToCluster() != null) {
                int initialSeverity = first.getToCluster().getSeverityLevel().getLevel();
                int currentSeverity = latest.getToCluster().getSeverityLevel().getLevel();
                String trajectory = currentSeverity < initialSeverity ? "IMPROVING" :
                        currentSeverity > initialSeverity ? "WORSENING" : "STABLE";
                analysis.put("overallTrajectory", trajectory);
                analysis.put("severityChange", currentSeverity - initialSeverity);
            }

            // Average time in clusters
            Double avgDays = transitionRepository.getAverageDaysInCluster();
            analysis.put("avgDaysInCluster", avgDays);

            // Journey timeline
            List<Map<String, Object>> timeline = transitions.stream()
                    .map(t -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("date", t.getTransitionDate());
                        point.put("toCluster", t.getToCluster() != null ?
                                t.getToCluster().getClusterIdentifier() : null);
                        point.put("type", t.getTransitionType());
                        point.put("direction", t.getTransitionDirection());
                        return point;
                    })
                    .collect(Collectors.toList());
            analysis.put("timeline", timeline);
        }

        return analysis;
    }

    @Override
    public Map<String, Object> getClusterPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // High performing clusters
        List<ClusterGroup> highPerforming = clusterRepository.findHighPerformingClusters(0.2);
        metrics.put("highPerformingClusters", highPerforming.stream()
                .map(ClusterGroupDTO::fromEntity).collect(Collectors.toList()));

        // Low performing clusters
        List<ClusterGroup> lowPerforming = clusterRepository.findLowPerformingClusters(0.05);
        metrics.put("lowPerformingClusters", lowPerforming.stream()
                .map(ClusterGroupDTO::fromEntity).collect(Collectors.toList()));

        // Improvement rate by category
        List<Object[]> improvementByCategory = clusterRepository.getImprovementRateByCategory();
        Map<String, Double> categoryImprovement = new HashMap<>();
        for (Object[] row : improvementByCategory) {
            categoryImprovement.put(row[0].toString(), (Double) row[1]);
        }
        metrics.put("improvementRateByCategory", categoryImprovement);

        return metrics;
    }

    @Override
    public Map<String, Object> getHighSeverityStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<ClusterGroup> highSeverity = clusterRepository.findHighSeverityClusters();
        stats.put("highSeverityClusters", highSeverity.stream()
                .map(ClusterGroupDTO::fromEntity).collect(Collectors.toList()));
        stats.put("totalHighSeverityMembers", highSeverity.stream()
                .mapToInt(c -> Math.toIntExact(c.getActiveMemberCount() != null ? c.getActiveMemberCount() : 0)).sum());

        List<ClusterGroup> critical = clusterRepository.findCriticalClusters();
        stats.put("criticalClusters", critical.stream()
                .map(ClusterGroupDTO::fromEntity).collect(Collectors.toList()));

        return stats;
    }

    // ==================== COMMUNITY FEATURES ====================

    @Override
    public Map<String, Object> getClusterMemberStatistics(Long clusterId) {
        ClusterGroup cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("memberCount", cluster.getMemberCount());
        stats.put("activeMemberCount", cluster.getActiveMemberCount());
        stats.put("avgStress", cluster.getAvgMemberStress());
        stats.put("avgDepression", cluster.getAvgMemberDepression());
        stats.put("avgAnxiety", cluster.getAvgMemberAnxiety());
        stats.put("avgResilience", cluster.getAvgMemberResilience());
        stats.put("stdDevStress", cluster.getStdDevStress());
        stats.put("stdDevDepression", cluster.getStdDevDepression());
        stats.put("stdDevAnxiety", cluster.getStdDevAnxiety());

        // Note: Individual member data is NOT returned to protect privacy
        return stats;
    }

    @Override
    public List<ClusterGroupDTO> getClustersWithPeerSupport() {
        return clusterRepository.findClustersWithPeerSupport().stream()
                .map(ClusterGroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getClusterRecommendedInterventions(Long clusterId) {
        return clusterRepository.findById(clusterId)
                .map(c -> parseJsonArray(c.getRecommendedInterventions()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<String> getClusterCopingStrategies(Long clusterId) {
        return clusterRepository.findById(clusterId)
                .map(c -> parseJsonArray(c.getCopingStrategies()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<String> getClusterSharedExperiences(Long clusterId) {
        return clusterRepository.findById(clusterId)
                .map(c -> parseJsonArray(c.getSharedExperiences()))
                .orElse(Collections.emptyList());
    }

    // ==================== MODEL MANAGEMENT ====================

    @Override
    public boolean triggerModelUpdate() {
        try {
            return mlServiceClient.triggerModelRetraining("clustering", Map.of());
        } catch (Exception e) {
            logger.error("Failed to trigger model update: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getAvailableModelVersions() {
        return clusterRepository.findDistinctModelVersions();
    }

    @Override
    public List<ClusterGroupDTO> updateClustersFromMLService(Map<String, Object> mlResponse) {
        List<ClusterGroupDTO> updated = new ArrayList<>();

        if (mlResponse.containsKey("clusters")) {
            List<Map<String, Object>> clusters = (List<Map<String, Object>>) mlResponse.get("clusters");
            String modelVersion = (String) mlResponse.getOrDefault("modelVersion", "unknown");

            for (Map<String, Object> clusterData : clusters) {
                String identifier = (String) clusterData.get("identifier");

                ClusterGroup cluster = clusterRepository.findByClusterIdentifier(identifier)
                        .orElse(new ClusterGroup());

                cluster.setClusterIdentifier(identifier);
                cluster.setCentroidStress((Double) clusterData.get("centroidStress"));
                cluster.setCentroidDepression((Double) clusterData.get("centroidDepression"));
                cluster.setCentroidAnxiety((Double) clusterData.get("centroidAnxiety"));
                cluster.setClusterWeight((Double) clusterData.get("weight"));
                cluster.setSilhouetteScore((Double) clusterData.get("silhouetteScore"));
                cluster.setModelVersion(modelVersion);
                cluster.setLastModelUpdate(LocalDateTime.now());
                cluster.setIsActive(true);

                ClusterGroup saved = clusterRepository.save(cluster);
                updated.add(ClusterGroupDTO.fromEntity(saved));
            }
        }

        return updated;
    }

    @Override
    public List<ClusterGroupDTO> initializeDefaultClusters() {
        List<ClusterGroupDTO> created = new ArrayList<>();

        // Create clusters for each category-severity combination
        for (ClusterCategory category : ClusterCategory.values()) {
            if (category == ClusterCategory.CRISIS) continue; // Handle crisis separately

            for (SeverityLevel severity : SeverityLevel.values()) {
                String identifier = category.name() + "_" + severity.name();

                if (clusterRepository.findByClusterIdentifier(identifier).isEmpty()) {
                    ClusterGroup cluster = new ClusterGroup(identifier, category, severity);

                    // Set default centroids based on severity
                    double baseLine = (severity.getMinScore() + severity.getMaxScore()) / 2;
                    switch (category) {
                        case STRESS -> {
                            cluster.setCentroidStress(baseLine);
                            cluster.setCentroidDepression(baseLine * 0.7);
                            cluster.setCentroidAnxiety(baseLine * 0.7);
                        }
                        case DEPRESSION -> {
                            cluster.setCentroidStress(baseLine * 0.7);
                            cluster.setCentroidDepression(baseLine);
                            cluster.setCentroidAnxiety(baseLine * 0.7);
                        }
                        case ANXIETY -> {
                            cluster.setCentroidStress(baseLine * 0.7);
                            cluster.setCentroidDepression(baseLine * 0.7);
                            cluster.setCentroidAnxiety(baseLine);
                        }
                        case MIXED -> {
                            cluster.setCentroidStress(baseLine);
                            cluster.setCentroidDepression(baseLine);
                            cluster.setCentroidAnxiety(baseLine);
                        }
                        case RESILIENT -> {
                            cluster.setCentroidStress(0.2);
                            cluster.setCentroidDepression(0.2);
                            cluster.setCentroidAnxiety(0.2);
                            cluster.setCentroidResilience(0.8);
                        }
                    }

                    cluster.setModelVersion("default-v1.0");
                    cluster.setLastModelUpdate(LocalDateTime.now());

                    ClusterGroup saved = clusterRepository.save(cluster);
                    created.add(ClusterGroupDTO.fromEntity(saved));
                }
            }
        }

        // Create CRISIS cluster
        if (clusterRepository.findByClusterIdentifier("CRISIS_CRITICAL").isEmpty()) {
            ClusterGroup crisisCluster = new ClusterGroup("CRISIS_CRITICAL",
                    ClusterCategory.CRISIS, SeverityLevel.SEVERE);
            crisisCluster.setCentroidStress(0.9);
            crisisCluster.setCentroidDepression(0.9);
            crisisCluster.setCentroidAnxiety(0.9);
            crisisCluster.setModelVersion("default-v1.0");
            crisisCluster.setLastModelUpdate(LocalDateTime.now());

            ClusterGroup saved = clusterRepository.save(crisisCluster);
            created.add(ClusterGroupDTO.fromEntity(saved));
        }

        logger.info("Initialized {} default clusters", created.size());
        return created;
    }

    @Override
    public boolean isClusteringServiceHealthy() {
        return mlServiceClient.isClusteringServiceHealthy();
    }

    // ==================== ALERTS AND MONITORING ====================

    @Override
    public List<ClusterGroupDTO> getClustersRequiringAttention() {
        return clusterRepository.findHighSeverityClusters().stream()
                .filter(ClusterGroup::needsAttention)
                .map(ClusterGroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterGroupDTO> getCriticalClusters() {
        return clusterRepository.findCriticalClusters().stream()
                .map(ClusterGroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClusterGroupDTO> getClustersNeedingReview() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return clusterRepository.findClustersNeedingReview(thirtyDaysAgo).stream()
                .map(ClusterGroupDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ClusterGroupDTO markClusterAsReviewed(Long clusterId, String reviewedBy) {
        ClusterGroup cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));

        cluster.setLastReviewed(LocalDateTime.now());
        cluster.setReviewedBy(reviewedBy);
        cluster.setRequiresReview(false);

        ClusterGroup saved = clusterRepository.save(cluster);
        return ClusterGroupDTO.fromEntity(saved);
    }

    // ==================== HELPER METHODS ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private ClusterAssignmentResult performClusterAssignment(Double stressScore, Double depressionScore,
                                                             Double anxietyScore) {
        // Try ML service first
        if (mlServiceClient.isClusteringServiceHealthy()) {
            try {
                Optional<Map<String, Object>> mlResult = mlServiceClient.getClusterAssignment(
                        stressScore, depressionScore, anxietyScore);

                if (mlResult.isPresent()) {
                    Map<String, Object> result = mlResult.get();
                    String identifier = (String) result.get("clusterIdentifier");

                    ClusterGroup cluster = clusterRepository.findByClusterIdentifier(identifier).orElse(null);

                    return ClusterAssignmentResult.builder()
                            .clusterIdentifier(identifier)
                            .primaryCategory((String) result.get("primaryCategory"))
                            .severityLevel((String) result.get("primaryLevel"))
                            .assignmentConfidence((Double) result.getOrDefault("confidence", 0.8))
                            .modelVersion((String) result.get("modelVersion"))
                            .usedFallback(false)
                            .clusterId(cluster != null ? cluster.getId() : null)
                            .clusterName(cluster != null ? cluster.getClusterName() : identifier)
                            .build();
                }
            } catch (Exception e) {
                logger.warn("ML cluster assignment failed, using fallback: {}", e.getMessage());
            }
        }

        // Fallback: rule-based assignment
        return performFallbackAssignment(stressScore, depressionScore, anxietyScore);
    }

    private ClusterAssignmentResult performFallbackAssignment(Double stressScore, Double depressionScore,
                                                              Double anxietyScore) {
        ClusterCategory category = determineCategory(stressScore, depressionScore, anxietyScore);
        SeverityLevel severity = determineSeverity(stressScore, depressionScore, anxietyScore);
        String identifier = category.name() + "_" + severity.name();

        ClusterGroup cluster = clusterRepository.findByClusterIdentifier(identifier).orElse(null);

        // Calculate distance if cluster exists
        Double distance = null;
        if (cluster != null) {
            distance = cluster.calculateDistanceFromCentroid(stressScore, depressionScore, anxietyScore);
        }

        return ClusterAssignmentResult.builder()
                .clusterIdentifier(identifier)
                .clusterId(cluster != null ? cluster.getId() : null)
                .clusterName(cluster != null ? cluster.getClusterName() :
                        category.getDisplayName() + " - " + severity.getDisplayName())
                .primaryCategory(category.name())
                .severityLevel(severity.name())
                .assignmentConfidence(0.7)
                .distanceToCentroid(distance)
                .modelVersion("fallback-v1.0")
                .usedFallback(true)
                .build();
    }

    private ClusterCategory determineCategory(Double stress, Double depression, Double anxiety) {
        double max = Math.max(stress, Math.max(depression, anxiety));
        double min = Math.min(stress, Math.min(depression, anxiety));

        // Check for crisis
        if (max >= 0.85) {
            return ClusterCategory.CRISIS;
        }

        // Check for resilient (all low)
        if (max < 0.3) {
            return ClusterCategory.RESILIENT;
        }

        // Check for mixed (all similar)
        if (max - min < 0.15) {
            return ClusterCategory.MIXED;
        }

        // Determine dominant category
        if (stress == max) return ClusterCategory.STRESS;
        if (depression == max) return ClusterCategory.DEPRESSION;
        return ClusterCategory.ANXIETY;
    }

    private SeverityLevel determineSeverity(Double stress, Double depression, Double anxiety) {
        double avgScore = (stress + depression + anxiety) / 3.0;
        return SeverityLevel.fromScore(avgScore);
    }

    private ClusterGroup createClusterFromResult(ClusterAssignmentResult result) {
        ClusterCategory category = ClusterCategory.valueOf(result.getPrimaryCategory());
        SeverityLevel severity = SeverityLevel.valueOf(result.getSeverityLevel());

        ClusterGroup cluster = new ClusterGroup(result.getClusterIdentifier(), category, severity);
        cluster.setModelVersion(result.getModelVersion());
        cluster.setLastModelUpdate(LocalDateTime.now());

        return clusterRepository.save(cluster);
    }

    private void updateClusterFromDTO(ClusterGroup cluster, ClusterGroupDTO dto) {
        if (dto.getClusterIdentifier() != null) cluster.setClusterIdentifier(dto.getClusterIdentifier());
        if (dto.getClusterName() != null) cluster.setClusterName(dto.getClusterName());
        if (dto.getDescription() != null) cluster.setDescription(dto.getDescription());
        if (dto.getPrimaryCategory() != null) cluster.setPrimaryCategory(dto.getPrimaryCategory());
        if (dto.getSeverityLevel() != null) cluster.setSeverityLevel(dto.getSeverityLevel());
        if (dto.getCentroidStress() != null) cluster.setCentroidStress(dto.getCentroidStress());
        if (dto.getCentroidDepression() != null) cluster.setCentroidDepression(dto.getCentroidDepression());
        if (dto.getCentroidAnxiety() != null) cluster.setCentroidAnxiety(dto.getCentroidAnxiety());
        if (dto.getCentroidResilience() != null) cluster.setCentroidResilience(dto.getCentroidResilience());
        if (dto.getMaxCapacity() != null) cluster.setMaxCapacity(dto.getMaxCapacity());
        if (dto.getRecommendedInterventions() != null) cluster.setRecommendedInterventions(dto.getRecommendedInterventions());
        if (dto.getPrimaryInterventionType() != null) cluster.setPrimaryInterventionType(dto.getPrimaryInterventionType());
        if (dto.getHasPeerSupport() != null) cluster.setHasPeerSupport(dto.getHasPeerSupport());
        if (dto.getCopingStrategies() != null) cluster.setCopingStrategies(dto.getCopingStrategies());
        if (dto.getSharedExperiences() != null) cluster.setSharedExperiences(dto.getSharedExperiences());
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        // Simple parsing - in production use Jackson
        return Arrays.stream(json.replace("[", "").replace("]", "")
                        .replace("\"", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}