package com.reserch.mano.service;

import java.util.Map;

public interface MLModelService {

    Map<String, Object> generateSyntheticData(Map<String, Object> inputData);

    Map<String, Double> predictRiskScores(Map<String, Object> userData);

    Map<String, Double> analyzeSentiment(String text);

    Map<String, Object> clusterUsers(Long userId);

    boolean isModelAvailable(String modelName);

    void updateModelStatus(String modelName, boolean isAvailable);

    Map<String, String> getModelStatuses();

    void trainModel(String modelName, Map<String, Object> trainingData);

    Map<String, Object> getModelMetrics(String modelName);
}

