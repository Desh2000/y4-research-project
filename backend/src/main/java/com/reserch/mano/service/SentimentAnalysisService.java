package com.reserch.mano.service;

import java.util.Map;

public interface SentimentAnalysisService {

    Double analyzeSentiment(String text);

    Map<String, Double> getDetailedSentiment(String text);

    boolean detectCrisisIndicators(String text);

    Map<String, Object> extractEmotions(String text);

    Double calculateRiskScore(String text, Map<String, Object> userContext);

    boolean requiresIntervention(String text, Double sentimentScore, Map<String, Object> userProfile);

    String generateSupportResponse(String userMessage, Map<String, Object> userContext);

    void updateSentimentModel(Map<String, Object> trainingData);

    boolean isModelHealthy();
}
