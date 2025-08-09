package com.reserch.mano.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stress_level")
    private Integer stressLevel; // 1-10 scale

    @Column(name = "anxiety_level")
    private Integer anxietyLevel; // 1-10 scale

    @Column(name = "mood_rating")
    private Integer moodRating; // 1-10 scale

    @Column(name = "sleep_quality")
    private Integer sleepQuality; // 1-10 scale

    @Column(name = "activity_level")
    private Integer activityLevel; // 1-10 scale

    @Column(name = "resilience_score")
    private Double resilienceScore;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "privacy_consent")
    private Boolean privacyConsent = false;

    @Column(name = "data_sharing_consent")
    private Boolean dataSharingConsent = false;

    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    private String notificationPreferences; // JSON string

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-One relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
