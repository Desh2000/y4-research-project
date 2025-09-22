package com.reserch.mano.repository;

import com.reserch.mano.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Prediction entity.
 */
@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
}
