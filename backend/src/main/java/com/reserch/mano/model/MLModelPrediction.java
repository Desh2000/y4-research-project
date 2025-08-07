package com.reserch.mano.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ML Model Prediction Entity for storing prediction results
 */
@Entity
@Table(name = "ml_model_predictions",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_model_name", columnList = "model_name"),
                @Index(name = "idx_prediction_date", columnList = "prediction_date"),
                @Index(name = "idx_status", columnList = "status")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MLModelPrediction extends BaseEntity {

    @NotBlank
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName; // e.g., "heart_disease_model", "diabetes_model"

    @NotBlank
    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion; // e.g., "v1.0", "v2.1"

    @Column(name = "prediction_date", nullable = false)
    private LocalDateTime predictionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PredictionStatus status = PredictionStatus.PENDING;

    // Input data as JSON
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    // Prediction result as JSON
    @Column(name = "prediction_result", columnDefinition = "TEXT")
    private String predictionResult;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Additional metadata as JSON
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructor for creating prediction with basic info
    public MLModelPrediction(String modelName, String modelVersion, String inputData, User user) {
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.inputData = inputData;
        this.user = user;
        this.predictionDate = LocalDateTime.now();
        this.status = PredictionStatus.PENDING;
    }

    // Enum for prediction status
    public enum PredictionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // Helper methods
    public boolean isCompleted() {
        return status == PredictionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PredictionStatus.FAILED;
    }

    public boolean isPending() {
        return status == PredictionStatus.PENDING;
    }

    public void markAsCompleted(String result, BigDecimal confidence, Long processingTime) {
        this.status = PredictionStatus.COMPLETED;
        this.predictionResult = result;
        this.confidenceScore = confidence;
        this.processingTimeMs = processingTime;
    }

    public void markAsFailed(String errorMessage) {
        this.status = PredictionStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markAsProcessing() {
        this.status = PredictionStatus.PROCESSING;
    }
}