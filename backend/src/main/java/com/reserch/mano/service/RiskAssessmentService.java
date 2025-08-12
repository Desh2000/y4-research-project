package com.reserch.mano.service;

import java.util.List;
import java.util.Map;

public interface RiskAssessmentService {

    Double calculateOverallRiskScore(Long userId);

    Map<String, Double> calculateRiskFactors(Long userId);

    Double predictStressLevel(Map<String, Object> userMetrics);

    Double predictCognitiveRisk(Map<String, Object> multimodalData);

    Map<String, Double> generateRiskPrediction(Long userId, int daysAhead);

    List<String> identifyRiskFactors(Long userId);

    Map<String, Object> getPersonalizedRecommendations(Long userId);

    boolean isHighRiskUser(Long userId, Double threshold);

    void updateUserRiskScore(Long userId, Double riskScore);

    Map<String, Double> getBenchmarkScores();

    void trainRiskModel(List<Map<String, Object>> trainingData);

    boolean isModelAccurate();
}