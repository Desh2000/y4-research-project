package com.reserch.mano.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a prediction result (e.g., stress level, cognitive risk) for a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A prediction belongs to one user.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "prediction_type", nullable = false)
    private String predictionType; // e.g., "STRESS_PREDICTION", "COGNITIVE_RISK"

    @Column(name = "prediction_score", nullable = false)
    private double stressScore;

    @Column(name = "prediction_value", nullable = false)
    private double cognitiveRiskScore;

    @Column(name = "prediction_summary", nullable = false)
    private String summary;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime PredictedAt;
}