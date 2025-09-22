package com.reserch.mano.service.serviceImpl.impl;

import com.reserch.mano.controller.dto.request.CreatePredictionRequest;
import com.reserch.mano.controller.dto.request.PredictionDto;
import com.reserch.mano.model.Prediction;
import com.reserch.mano.model.User;
import com.reserch.mano.repository.PredictionRepository;
import com.reserch.mano.service.serviceImpl.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for prediction-related operations.
 */
@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;

    @Override
    public PredictionDto createPrediction(CreatePredictionRequest request, User user) {
        Prediction prediction = Prediction.builder()
                .user(user)
                .stressScore(request.getStressScore())
                .cognitiveRiskScore(request.getCognitiveRiskScore())
                .summary(request.getSummary())
                .build();

        Prediction savedPrediction = predictionRepository.save(prediction);

        return mapToDto(savedPrediction);
    }

    @Override
    public List<PredictionDto> getPredictionsForUser(User user) {
        return predictionRepository.findAll().stream()
                .filter(prediction -> prediction.getUser().getId().equals(user.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PredictionDto mapToDto(Prediction prediction) {
        return PredictionDto.builder()
                .id(prediction.getId())
                .stressScore(prediction.getStressScore())
                .cognitiveRiskScore(prediction.getCognitiveRiskScore())
                .summary(prediction.getSummary())
                .predictedAt(prediction.getPredictedAt())
                .userId(prediction.getUser().getId())
                .build();
    }
}