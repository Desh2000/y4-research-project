package com.reserch.mano.controller.dto;

import com.reserch.mano.controller.dto.request.CreatePredictionRequest;
import com.reserch.mano.controller.dto.request.PredictionDto;
import com.reserch.mano.model.User;
import com.reserch.mano.service.serviceImpl.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling prediction-related API requests.
 */
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    /**
     * Endpoint to create a new prediction for the authenticated user.
     * Note: In a real application, this might be a protected, internal endpoint
     * called by a background job after running the ML model.
     * @param request The request body containing prediction details.
     * @param user The currently authenticated user.
     * @return The created prediction DTO.
     */
    @PostMapping
    public ResponseEntity<PredictionDto> createPrediction(@RequestBody CreatePredictionRequest request, @AuthenticationPrincipal User user) {
        PredictionDto createdPrediction = predictionService.createPrediction(request, user);
        return new ResponseEntity<>(createdPrediction, HttpStatus.CREATED);
    }

    /**
     * Endpoint to get all predictions for the authenticated user.
     * @param user The currently authenticated user.
     * @return A list of prediction DTOs.
     */
    @GetMapping
    public ResponseEntity<List<PredictionDto>> getPredictionsForUser(@AuthenticationPrincipal User user) {
        List<PredictionDto> predictions = predictionService.getPredictionsForUser(user);
        return ResponseEntity.ok(predictions);
    }
}
