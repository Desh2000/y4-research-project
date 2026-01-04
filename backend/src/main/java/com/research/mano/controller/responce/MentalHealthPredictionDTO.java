package com.research.mano.controller.responce;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MentalHealthPredictionDTO {
    private Long id;
    private Long userId;
    private String username;

    @NotNull(message = "Stress score is required")
    @DecimalMin(value = "0.0", message = "Stress score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Stress score must be between 0.0 and 1.0")
    private Double stressScore;

    @NotNull(message = "Depression score is required")
    @DecimalMin(value = "0.0", message = "Depression score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Depression score must be between 0.0 and 1.0")
    private Double depressionScore;

    @NotNull(message = "Anxiety score is required")
    @DecimalMin(value = "0.0", message = "Anxiety score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Anxiety score must be between 0.0 and 1.0")
    private Double anxietyScore;

    private Double overallRiskScore;
    private LocalDateTime predictionDate;
    private String modelVersion;
    private String dataSource;


    private String primaryClusterCategory;
    private String primaryClusterLevel;
    private LocalDateTime clusterAssignmentDate;
}
