package com.reserch.mano.repository;

import com.reserch.mano.model.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for HealthMetric entity.
 * Provides CRUD operations for health metrics.
 */
@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    // Spring Data JPA will automatically provide methods like:
    // - save(HealthMetric metric)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // We can add custom query methods here later if needed,
    // for example, to find all metrics for a specific user.
}
