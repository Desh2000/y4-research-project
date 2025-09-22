package com.reserch.mano.service.serviceImpl;

import com.reserch.mano.controller.dto.request.CreatePredictionRequest;
import com.reserch.mano.controller.dto.request.PredictionDto;
import com.reserch.mano.model.User;

import java.util.List;

/**
 * Service interface for prediction-related operations.
 */
public interface PredictionService {
    PredictionDto createPrediction(CreatePredictionRequest request, User user);
    List<PredictionDto> getPredictionsForUser(User user);
}