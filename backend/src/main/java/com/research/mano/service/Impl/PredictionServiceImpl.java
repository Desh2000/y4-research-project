package com.research.mano.service.Impl;

import com.research.mano.controller.request.PredictionRequest;
import com.research.mano.controller.responce.PredictionResultDTO;
import com.research.mano.dto.ml.PredictionInput;
import com.research.mano.dto.ml.PredictionOutput;
import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.entity.User;
import com.research.mano.repository.MentalHealthPredictionRepository;
import com.research.mano.repository.UserRepository;
import com.research.mano.service.PredictionService;
import com.research.mano.service.UserProfileService;
import com.research.mano.service.ml.MLServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced Prediction Service Implementation
 * Integrates ML service with database storage
 */
@Service
@Transactional
public class PredictionServiceImpl implements PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionServiceImpl.class);

    @Autowired
    private MentalHealthPredictionRepository predictionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private MLServiceClient mlServiceClient;

    // ==================== PRIMARY PREDICTION OPERATIONS ====================

    @Override
    public PredictionResultDTO createPrediction(Long userId, PredictionRequest request) {
        User user = findUserById(userId);

        // Check if direct scores provided
        if (request.hasDirectScores()) {
            return createDirectPrediction(userId,
                    request.getStressScore(),
                    request.getDepressionScore(),
                    request.getAnxietyScore(),
                    request.getDataSource() != null ? request.getDataSource() : "direct_input");
        }

        // Convert to ML input and call service
        PredictionInput mlInput = request.toPredictionInput(userId);

        // Enrich with user's historical data
        enrichWithHistoricalData(mlInput, user);

        Optional<PredictionOutput> mlOutput = mlServiceClient.getPrediction(mlInput);

        if (mlOutput.isPresent()) {
            PredictionOutput output = mlOutput.get();

            // Store prediction
            MentalHealthPrediction stored = storePrediction(user, output, request);

            // Update user profile
            updateUserProfile(userId, output);

            // Create result DTO
            PredictionResultDTO result = PredictionResultDTO.fromMLOutput(output, stored.getId());

            // Add questionnaire scores if available
            enrichWithQuestionnaireScores(result, request);

            return result;
        }

        // Fallback: create from questionnaire data
        return createFromQuestionnairesFallback(userId, request);
    }

    @Override
    public PredictionResultDTO createDirectPrediction(Long userId, Double stressScore,
                                                      Double depressionScore, Double anxietyScore,
                                                      String dataSource) {
        validateScores(stressScore, depressionScore, anxietyScore);

        User user = findUserById(userId);

        MentalHealthPrediction prediction = new MentalHealthPrediction(user, stressScore, depressionScore, anxietyScore);
        prediction.setDataSource(dataSource != null ? dataSource : "direct_input");
        prediction.setModelVersion("direct");
        prediction.assignCluster();

        MentalHealthPrediction saved = predictionRepository.save(prediction);

        // Update user profile
        userProfileService.updateCurrentScores(userId, stressScore, anxietyScore, depressionScore);

        return PredictionResultDTO.fromEntity(saved);
    }

    @Override
    public Optional<PredictionOutput> getMLPrediction(PredictionInput input) {
        return mlServiceClient.getPrediction(input);
    }

    @Override
    public PredictionResultDTO rerunPrediction(Long predictionId, String modelVersion) {
        MentalHealthPrediction existing = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found: " + predictionId));

        // Create input from existing prediction
        PredictionInput input = new PredictionInput();
        input.setUserId(existing.getUser().getId());
        input.setModelVersion(modelVersion);

        // Use stored input features if available
        if (existing.getInputFeatures() != null) {
            // Parse and apply stored features
        }

        Optional<PredictionOutput> mlOutput = mlServiceClient.getPredictionWithModel(input, modelVersion);

        if (mlOutput.isPresent()) {
            PredictionOutput output = mlOutput.get();
            MentalHealthPrediction stored = storePrediction(existing.getUser(), output, null);
            return PredictionResultDTO.fromMLOutput(output, stored.getId());
        }

        throw new RuntimeException("Failed to rerun prediction with model: " + modelVersion);
    }

    // ==================== QUESTIONNAIRE-BASED PREDICTIONS ====================

    @Override
    public PredictionResultDTO createFromPHQ9(Long userId, List<Integer> responses) {
        validatePHQ9Responses(responses);

        int total = responses.stream().mapToInt(Integer::intValue).sum();
        double depressionScore = Math.min(1.0, total / 27.0);

        PredictionResultDTO result = createDirectPrediction(userId, 0.5, depressionScore, 0.5, "phq9");
        result.setPhq9TotalScore(total);
        result.setPhq9Severity(PredictionResultDTO.getPHQ9Severity(total));

        return result;
    }

    @Override
    public PredictionResultDTO createFromGAD7(Long userId, List<Integer> responses) {
        validateGAD7Responses(responses);

        int total = responses.stream().mapToInt(Integer::intValue).sum();
        double anxietyScore = Math.min(1.0, total / 21.0);

        PredictionResultDTO result = createDirectPrediction(userId, 0.5, 0.5, anxietyScore, "gad7");
        result.setGad7TotalScore(total);
        result.setGad7Severity(PredictionResultDTO.getGAD7Severity(total));

        return result;
    }

    @Override
    public PredictionResultDTO createFromPSS(Long userId, List<Integer> responses) {
        validatePSSResponses(responses);

        int total = responses.stream().mapToInt(Integer::intValue).sum();
        double stressScore = Math.min(1.0, total / 40.0);

        PredictionResultDTO result = createDirectPrediction(userId, stressScore, 0.5, 0.5, "pss");
        result.setPssTotalScore(total);
        result.setPssSeverity(PredictionResultDTO.getPSSSeverity(total));

        return result;
    }

    @Override
    public PredictionResultDTO createFromQuestionnaires(Long userId,
                                                        List<Integer> phq9,
                                                        List<Integer> gad7,
                                                        List<Integer> pss) {
        Double stressScore = 0.5;
        Double depressionScore = 0.5;
        Double anxietyScore = 0.5;

        Integer phq9Total = null, gad7Total = null, pssTotal = null;

        if (phq9 != null && phq9.size() == 9) {
            phq9Total = phq9.stream().mapToInt(Integer::intValue).sum();
            depressionScore = Math.min(1.0, phq9Total / 27.0);
        }

        if (gad7 != null && gad7.size() == 7) {
            gad7Total = gad7.stream().mapToInt(Integer::intValue).sum();
            anxietyScore = Math.min(1.0, gad7Total / 21.0);
        }

        if (pss != null && pss.size() == 10) {
            pssTotal = pss.stream().mapToInt(Integer::intValue).sum();
            stressScore = Math.min(1.0, pssTotal / 40.0);
        }

        PredictionResultDTO result = createDirectPrediction(userId, stressScore, depressionScore, anxietyScore, "questionnaires");

        result.setPhq9TotalScore(phq9Total);
        result.setPhq9Severity(PredictionResultDTO.getPHQ9Severity(phq9Total));
        result.setGad7TotalScore(gad7Total);
        result.setGad7Severity(PredictionResultDTO.getGAD7Severity(gad7Total));
        result.setPssTotalScore(pssTotal);
        result.setPssSeverity(PredictionResultDTO.getPSSSeverity(pssTotal));

        return result;
    }

    // ==================== RETRIEVAL OPERATIONS ====================

    @Override
    public Optional<PredictionResultDTO> getPredictionById(Long predictionId) {
        return predictionRepository.findById(predictionId)
                .map(PredictionResultDTO::fromEntity);
    }

    @Override
    public Optional<PredictionResultDTO> getLatestPrediction(Long userId) {
        return predictionRepository.findLatestByUserId(userId)
                .map(PredictionResultDTO::fromEntity);
    }

    @Override
    public List<PredictionResultDTO> getUserPredictionHistory(Long userId) {
        User user = findUserById(userId);
        return predictionRepository.findByUserOrderByPredictionDateDesc(user).stream()
                .map(PredictionResultDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionResultDTO> getUserPredictionHistory(Long userId, int page, int size) {
        User user = findUserById(userId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "predictionDate"));

        return predictionRepository.findByUserOrderByPredictionDateDesc(user).stream()
                .skip((long) page * size)
                .limit(size)
                .map(PredictionResultDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionResultDTO> getPredictionsByDateRange(Long userId,
                                                               LocalDateTime start,
                                                               LocalDateTime end) {
        User user = findUserById(userId);
        return predictionRepository.findByUserAndDateRange(user, start, end).stream()
                .map(PredictionResultDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== ANALYSIS OPERATIONS ====================

    @Override
    public Map<String, Object> getRiskProgression(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        User user = findUserById(userId);

        List<MentalHealthPrediction> predictions = predictionRepository
                .findByUserAndDateRange(user, startDate, LocalDateTime.now());

        Map<String, Object> progression = new HashMap<>();

        List<Map<String, Object>> dataPoints = predictions.stream()
                .map(p -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", p.getPredictionDate());
                    point.put("stressScore", p.getStressScore());
                    point.put("depressionScore", p.getDepressionScore());
                    point.put("anxietyScore", p.getAnxietyScore());
                    point.put("overallRisk", p.getOverallRiskScore());
                    return point;
                })
                .collect(Collectors.toList());

        progression.put("dataPoints", dataPoints);
        progression.put("periodDays", days);
        progression.put("totalPredictions", predictions.size());

        // Calculate trends
        if (predictions.size() >= 2) {
            MentalHealthPrediction first = predictions.get(predictions.size() - 1);
            MentalHealthPrediction last = predictions.get(0);

            progression.put("stressTrend", calculateTrend(first.getStressScore(), last.getStressScore()));
            progression.put("depressionTrend", calculateTrend(first.getDepressionScore(), last.getDepressionScore()));
            progression.put("anxietyTrend", calculateTrend(first.getAnxietyScore(), last.getAnxietyScore()));
        }

        return progression;
    }

    @Override
    public Map<String, Object> getTrendAnalysis(Long userId) {
        return getRiskProgression(userId, 30);
    }

    @Override
    public Map<String, Object> getClusterComparison(Long userId) {
        Optional<MentalHealthPrediction> latest = predictionRepository.findLatestByUserId(userId);

        Map<String, Object> comparison = new HashMap<>();

        if (latest.isPresent()) {
            MentalHealthPrediction pred = latest.get();
            String clusterId = pred.getClusterIdentifier();

            if (clusterId != null) {
                // Get cluster averages
                List<Object[]> clusterStats = predictionRepository.getAverageScoresByCluster(clusterId);

                if (!clusterStats.isEmpty()) {
                    Object[] stats = clusterStats.get(0);
                    comparison.put("clusterAvgStress", stats[0]);
                    comparison.put("clusterAvgDepression", stats[1]);
                    comparison.put("clusterAvgAnxiety", stats[2]);
                }

                comparison.put("userStress", pred.getStressScore());
                comparison.put("userDepression", pred.getDepressionScore());
                comparison.put("userAnxiety", pred.getAnxietyScore());
                comparison.put("clusterId", clusterId);
            }
        }

        return comparison;
    }

    @Override
    public Map<String, Object> getUserPredictionStatistics(Long userId) {
        User user = findUserById(userId);
        List<MentalHealthPrediction> predictions = predictionRepository.findByUserOrderByPredictionDateDesc(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPredictions", predictions.size());

        if (!predictions.isEmpty()) {
            DoubleSummaryStatistics stressStats = predictions.stream()
                    .filter(p -> p.getStressScore() != null)
                    .mapToDouble(MentalHealthPrediction::getStressScore)
                    .summaryStatistics();

            DoubleSummaryStatistics depressionStats = predictions.stream()
                    .filter(p -> p.getDepressionScore() != null)
                    .mapToDouble(MentalHealthPrediction::getDepressionScore)
                    .summaryStatistics();

            DoubleSummaryStatistics anxietyStats = predictions.stream()
                    .filter(p -> p.getAnxietyScore() != null)
                    .mapToDouble(MentalHealthPrediction::getAnxietyScore)
                    .summaryStatistics();

            stats.put("stressStats", Map.of(
                    "min", stressStats.getMin(),
                    "max", stressStats.getMax(),
                    "avg", stressStats.getAverage()
            ));
            stats.put("depressionStats", Map.of(
                    "min", depressionStats.getMin(),
                    "max", depressionStats.getMax(),
                    "avg", depressionStats.getAverage()
            ));
            stats.put("anxietyStats", Map.of(
                    "min", anxietyStats.getMin(),
                    "max", anxietyStats.getMax(),
                    "avg", anxietyStats.getAverage()
            ));

            stats.put("firstPrediction", predictions.get(predictions.size() - 1).getPredictionDate());
            stats.put("lastPrediction", predictions.get(0).getPredictionDate());
        }

        return stats;
    }

    // ==================== CLUSTER OPERATIONS ====================

    @Override
    public Map<String, Object> getClusterAssignment(Double stressScore,
                                                    Double depressionScore,
                                                    Double anxietyScore) {
        return mlServiceClient.getClusterAssignment(stressScore, depressionScore, anxietyScore)
                .orElseGet(() -> createFallbackClusterAssignment(stressScore, depressionScore, anxietyScore));
    }

    @Override
    public void updateUserCluster(Long userId, Long predictionId) {
        MentalHealthPrediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found: " + predictionId));

        String clusterId = prediction.getClusterIdentifier();
        if (clusterId != null) {
            userProfileService.updateClusterAssignment(userId, clusterId);
        }
    }

    // ==================== ALERT OPERATIONS ====================

    @Override
    public boolean requiresImmediateAttention(Long predictionId) {
        return predictionRepository.findById(predictionId)
                .map(p -> (p.getStressScore() != null && p.getStressScore() >= 0.8) ||
                        (p.getDepressionScore() != null && p.getDepressionScore() >= 0.8) ||
                        (p.getAnxietyScore() != null && p.getAnxietyScore() >= 0.8))
                .orElse(false);
    }

    @Override
    public List<PredictionResultDTO> getHighRiskPredictions(Double threshold) {
        return predictionRepository.findHighRiskPredictions(threshold).stream()
                .map(PredictionResultDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionResultDTO> getPredictionsRequiringFollowUp() {
        // Get predictions from last 24 hours with high risk
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        return predictionRepository.findRecentHighRiskPredictions(cutoff, 0.7).stream()
                .map(PredictionResultDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== ML SERVICE OPERATIONS ====================

    @Override
    public Map<String, Boolean> checkMLServiceHealth() {
        return mlServiceClient.getAllServicesHealth();
    }

    @Override
    public List<String> getAvailableModelVersions() {
        return mlServiceClient.getAvailableModelVersions();
    }

    @Override
    public Map<String, Object> getModelMetrics() {
        return mlServiceClient.getModelMetrics("lstm").orElse(Map.of());
    }

    // ==================== BATCH OPERATIONS ====================

    @Override
    public List<PredictionResultDTO> createBatchPredictions(List<PredictionRequest> requests, Long userId) {
        return requests.stream()
                .map(request -> createPrediction(userId, request))
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionResultDTO> recalculateUserPredictions(Long userId, String modelVersion) {
        List<MentalHealthPrediction> existing = predictionRepository.findByUserId(userId);

        return existing.stream()
                .map(pred -> {
                    try {
                        return rerunPrediction(pred.getId(), modelVersion);
                    } catch (Exception e) {
                        logger.error("Failed to recalculate prediction {}: {}", pred.getId(), e.getMessage());
                        return PredictionResultDTO.fromEntity(pred);
                    }
                })
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private void validateScores(Double stress, Double depression, Double anxiety) {
        if (stress == null || depression == null || anxiety == null) {
            throw new IllegalArgumentException("All scores must be provided");
        }
        if (stress < 0 || stress > 1 || depression < 0 || depression > 1 || anxiety < 0 || anxiety > 1) {
            throw new IllegalArgumentException("All scores must be between 0.0 and 1.0");
        }
    }

    private void validatePHQ9Responses(List<Integer> responses) {
        if (responses == null || responses.size() != 9) {
            throw new IllegalArgumentException("PHQ-9 requires exactly 9 responses");
        }
        if (responses.stream().anyMatch(r -> r < 0 || r > 3)) {
            throw new IllegalArgumentException("PHQ-9 responses must be between 0 and 3");
        }
    }

    private void validateGAD7Responses(List<Integer> responses) {
        if (responses == null || responses.size() != 7) {
            throw new IllegalArgumentException("GAD-7 requires exactly 7 responses");
        }
        if (responses.stream().anyMatch(r -> r < 0 || r > 3)) {
            throw new IllegalArgumentException("GAD-7 responses must be between 0 and 3");
        }
    }

    private void validatePSSResponses(List<Integer> responses) {
        if (responses == null || responses.size() != 10) {
            throw new IllegalArgumentException("PSS requires exactly 10 responses");
        }
        if (responses.stream().anyMatch(r -> r < 0 || r > 4)) {
            throw new IllegalArgumentException("PSS responses must be between 0 and 4");
        }
    }

    private MentalHealthPrediction storePrediction(User user, PredictionOutput output, PredictionRequest request) {
        MentalHealthPrediction prediction = new MentalHealthPrediction(
                user,
                output.getStressScore(),
                output.getDepressionScore(),
                output.getAnxietyScore()
        );

        prediction.setModelVersion(output.getModelVersion());
        prediction.setConfidenceLevel(output.getOverallConfidence());
        prediction.setDataSource(request != null && request.getDataSource() != null
                ? request.getDataSource() : "ml_service");

        prediction.assignCluster();

        return predictionRepository.save(prediction);
    }

    private void updateUserProfile(Long userId, PredictionOutput output) {
        try {
            userProfileService.updateCurrentScores(
                    userId,
                    output.getStressScore(),
                    output.getAnxietyScore(),
                    output.getDepressionScore()
            );

            if (output.getClusterIdentifier() != null) {
                userProfileService.updateClusterAssignment(userId, output.getClusterIdentifier());
            }

            // Check for high risk
            if (output.hasHighRisk()) {
                userProfileService.setHighRiskAlert(userId, true);
            }
        } catch (Exception e) {
            logger.error("Failed to update user profile for user {}: {}", userId, e.getMessage());
        }
    }

    private void enrichWithHistoricalData(PredictionInput input, User user) {
        // Add previous assessments from database
        List<MentalHealthPrediction> history = predictionRepository
                .findByUserOrderByPredictionDateDesc(user).stream()
                .limit(30)
                .toList();

        if (!history.isEmpty()) {
            input.setAssessmentHistory(history.stream()
                    .map(p -> PredictionInput.AssessmentEntry.builder()
                            .date(p.getPredictionDate().toString())
                            .stressScore(p.getStressScore())
                            .depressionScore(p.getDepressionScore())
                            .anxietyScore(p.getAnxietyScore())
                            .assessmentType(p.getDataSource())
                            .build())
                    .collect(Collectors.toList()));
        }
    }

    private void enrichWithQuestionnaireScores(PredictionResultDTO result, PredictionRequest request) {
        if (request.getPhq9Responses() != null && request.getPhq9Responses().size() == 9) {
            int total = request.getPhq9Responses().stream().mapToInt(Integer::intValue).sum();
            result.setPhq9TotalScore(total);
            result.setPhq9Severity(PredictionResultDTO.getPHQ9Severity(total));
        }

        if (request.getGad7Responses() != null && request.getGad7Responses().size() == 7) {
            int total = request.getGad7Responses().stream().mapToInt(Integer::intValue).sum();
            result.setGad7TotalScore(total);
            result.setGad7Severity(PredictionResultDTO.getGAD7Severity(total));
        }

        if (request.getPssResponses() != null && request.getPssResponses().size() == 10) {
            int total = request.getPssResponses().stream().mapToInt(Integer::intValue).sum();
            result.setPssTotalScore(total);
            result.setPssSeverity(PredictionResultDTO.getPSSSeverity(total));
        }
    }

    private PredictionResultDTO createFromQuestionnairesFallback(Long userId, PredictionRequest request) {
        return createFromQuestionnaires(
                userId,
                request.getPhq9Responses(),
                request.getGad7Responses(),
                request.getPssResponses()
        );
    }

    private Map<String, Object> createFallbackClusterAssignment(Double stress, Double depression, Double anxiety) {
        String category;
        double maxScore = Math.max(stress, Math.max(depression, anxiety));

        if (maxScore == stress) category = "STRESS";
        else if (maxScore == depression) category = "DEPRESSION";
        else category = "ANXIETY";

        String level;
        if (maxScore >= 0.7) level = "HIGH";
        else if (maxScore >= 0.4) level = "MEDIUM";
        else level = "LOW";

        return Map.of(
                "clusterIdentifier", category + "_" + level,
                "primaryCategory", category,
                "primaryLevel", level,
                "confidence", 0.7,
                "isFallback", true
        );
    }

    private String calculateTrend(Double first, Double last) {
        if (first == null || last == null) return "UNKNOWN";
        double diff = last - first;
        if (diff < -0.1) return "IMPROVING";
        if (diff > 0.1) return "WORSENING";
        return "STABLE";
    }
}