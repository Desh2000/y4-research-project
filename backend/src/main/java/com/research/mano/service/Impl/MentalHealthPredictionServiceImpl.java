package com.research.mano.service.Impl;


import com.research.mano.entity.MentalHealthPrediction;
import com.research.mano.entity.User;
import com.research.mano.repository.MentalHealthPredictionRepository;
import com.research.mano.service.MentalHealthPredictionService;
import com.research.mano.service.SystemAlertService;
import com.research.mano.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mental Health Prediction Service Implementation
 * Handles Component 2 (LSTM Prediction System) business logic
 */
@Service
@Transactional
public class MentalHealthPredictionServiceImpl implements MentalHealthPredictionService {

    @Autowired
    private MentalHealthPredictionRepository predictionRepository;

    @Autowired
    private SystemAlertService systemAlertService;

    @Autowired
    private UserProfileService userProfileService;

    @Override
    public MentalHealthPrediction save(MentalHealthPrediction prediction) {
        return predictionRepository.save(prediction);
    }

    @Override
    public List<MentalHealthPrediction> saveAll(List<MentalHealthPrediction> predictions) {
        return predictionRepository.saveAll(predictions);
    }

    @Override
    public Optional<MentalHealthPrediction> findById(Long id) {
        return predictionRepository.findById(id);
    }

    @Override
    public List<MentalHealthPrediction> findAll() {
        return predictionRepository.findAll();
    }

    @Override
    public Page<MentalHealthPrediction> findAll(Pageable pageable) {
        return predictionRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return predictionRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        predictionRepository.deleteById(id);
    }

    @Override
    public void delete(MentalHealthPrediction prediction) {
        predictionRepository.delete(prediction);
    }

    @Override
    public long count() {
        return predictionRepository.count();
    }

    @Override
    public MentalHealthPrediction createPrediction(User user, Double stressScore, Double depressionScore,
                                                   Double anxietyScore, String modelVersion, String dataSource) {
        if (!validatePredictionScores(stressScore, depressionScore, anxietyScore)) {
            throw new IllegalArgumentException("All scores must be between 0.0 and 1.0");
        }

        MentalHealthPrediction prediction = new MentalHealthPrediction(user, stressScore, depressionScore, anxietyScore);
        prediction.setModelVersion(modelVersion);
        prediction.setDataSource(dataSource);

        // Auto-assign cluster based on scores
        prediction.assignCluster();

        MentalHealthPrediction savedPrediction = predictionRepository.save(prediction);

        // Update user profile with latest scores
        userProfileService.updateCurrentScores(user.getId(), stressScore, anxietyScore, depressionScore);

        // Generate alerts if high risk
        if (isHighRisk(savedPrediction)) {
            generateHighRiskAlerts(List.of(savedPrediction));
        }

        return savedPrediction;
    }

    @Override
    public Optional<MentalHealthPrediction> getLatestPrediction(User user) {
        return predictionRepository.findLatestByUser(user);
    }

    @Override
    public Optional<MentalHealthPrediction> getLatestPrediction(Long userId) {
        return predictionRepository.findLatestByUserId(userId);
    }

    @Override
    public List<MentalHealthPrediction> getAllPredictions(User user) {
        return predictionRepository.findByUserOrderByPredictionDateDesc(user);
    }

    @Override
    public List<MentalHealthPrediction> getPredictionsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return predictionRepository.findByUserAndDateRange(user, startDate, endDate);
    }

    @Override
    public List<MentalHealthPrediction> findHighRiskPredictions() {
        return predictionRepository.findHighRiskPredictions();
    }

    @Override
    public List<MentalHealthPrediction> findByClusterCategory(MentalHealthPrediction.ClusterCategory category) {
        return predictionRepository.findByPrimaryClusterCategory(category);
    }

    @Override
    public List<MentalHealthPrediction> findByClusterLevel(MentalHealthPrediction.ClusterLevel level) {
        return predictionRepository.findByPrimaryClusterLevel(level);
    }

    @Override
    public List<MentalHealthPrediction> findPredictionsNeedingClustering() {
        return predictionRepository.findUnclusteredPredictions();
    }

    @Override
    public MentalHealthPrediction assignCluster(Long predictionId,
                                                MentalHealthPrediction.ClusterCategory category,
                                                MentalHealthPrediction.ClusterLevel level) {
        MentalHealthPrediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        prediction.setPrimaryClusterCategory(category);
        prediction.setPrimaryClusterLevel(level);
        prediction.setClusterAssignmentDate(LocalDateTime.now());

        return predictionRepository.save(prediction);
    }

    @Override
    public List<MentalHealthPrediction> processNewPredictionsForClustering() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(1); // Process predictions from last hour
        List<MentalHealthPrediction> newPredictions = predictionRepository.findRecentPredictionsForClustering(cutoffDate);

        for (MentalHealthPrediction prediction : newPredictions) {
            prediction.assignCluster();
        }

        return predictionRepository.saveAll(newPredictions);
    }

    @Override
    public List<Object[]> getPredictionStatsByCluster() {
        return predictionRepository.getPredictionStatsByCluster();
    }

    @Override
    public List<Object[]> getDailyPredictionCounts(LocalDateTime startDate) {
        return predictionRepository.getDailyPredictionCounts(startDate);
    }

    @Override
    public List<MentalHealthPrediction> findTrendingRiskIncreases() {
        LocalDateTime recentDate = LocalDateTime.now().minusDays(7); // Last week
        return predictionRepository.findTrendingRiskIncreases(recentDate);
    }

    @Override
    public List<Object[]> getUserRiskProgression(Long userId) {
        return predictionRepository.getUserRiskProgression(userId);
    }

    @Override
    public List<MentalHealthPrediction> findPredictionsForMLRetraining(int daysBack) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
        return predictionRepository.findPredictionsForMLRetraining(cutoffDate);
    }

    @Override
    public MentalHealthPrediction updatePredictionScores(Long predictionId, Double stressScore,
                                                         Double depressionScore, Double anxietyScore) {
        if (!validatePredictionScores(stressScore, depressionScore, anxietyScore)) {
            throw new IllegalArgumentException("All scores must be between 0.0 and 1.0");
        }

        MentalHealthPrediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        prediction.setStressScore(stressScore);
        prediction.setDepressionScore(depressionScore);
        prediction.setAnxietyScore(anxietyScore);
        prediction.setOverallRiskScore(prediction.calculateOverallRisk());

        // Reassign cluster based on new scores
        prediction.assignCluster();

        return predictionRepository.save(prediction);
    }

    @Override
    public MentalHealthPrediction calculateOverallRisk(Long predictionId) {
        MentalHealthPrediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        Double overallRisk = prediction.calculateOverallRisk();
        prediction.setOverallRiskScore(overallRisk);

        return predictionRepository.save(prediction);
    }

    @Override
    public List<MentalHealthPrediction> findByModelVersion(String modelVersion) {
        return predictionRepository.findByModelVersion(modelVersion);
    }

    @Override
    public void generateHighRiskAlerts(List<MentalHealthPrediction> predictions) {
        for (MentalHealthPrediction prediction : predictions) {
            if (isHighRisk(prediction)) {
                systemAlertService.createHighRiskPredictionAlert(
                        prediction.getUser(),
                        prediction.getOverallRiskScore(),
                        String.format("High risk prediction: Overall=%.2f, Stress=%.2f, Depression=%.2f, Anxiety=%.2f",
                                prediction.getOverallRiskScore(),
                                prediction.getStressScore(),
                                prediction.getDepressionScore(),
                                prediction.getAnxietyScore())
                );
            }
        }
    }

    @Override
    public boolean validatePredictionScores(Double stressScore, Double depressionScore, Double anxietyScore) {
        return isValidScore(stressScore) && isValidScore(depressionScore) && isValidScore(anxietyScore);
    }

    private boolean isValidScore(Double score) {
        return score != null && score >= 0.0 && score <= 1.0;
    }

    private boolean isHighRisk(MentalHealthPrediction prediction) {
        return prediction.getStressScore() >= 0.8 ||
                prediction.getDepressionScore() >= 0.8 ||
                prediction.getAnxietyScore() >= 0.8 ||
                (prediction.getOverallRiskScore() != null && prediction.getOverallRiskScore() >= 0.8);
    }

    @Override
    public List<Object[]> getAverageScoresByDate(LocalDateTime startDate) {
        return predictionRepository.getAverageScoresByDate(startDate);
    }
}
