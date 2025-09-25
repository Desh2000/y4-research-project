package com.research.mano.controller.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MentalHealthScoreUpdateRequest {
    @NotNull(message = "Stress score is required")
    @DecimalMin(value = "0.0", message = "Stress score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Stress score must be between 0.0 and 1.0")
    private Double stressScore;

    @NotNull(message = "Anxiety score is required")
    @DecimalMin(value = "0.0", message = "Anxiety score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Anxiety score must be between 0.0 and 1.0")
    private Double anxietyScore;

    @NotNull(message = "Depression score is required")
    @DecimalMin(value = "0.0", message = "Depression score must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Depression score must be between 0.0 and 1.0")
    private Double depressionScore;
}
