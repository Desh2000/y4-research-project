package com.research.mano.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base Repository Interface
 * Provides common repository methods for all entities
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    // Common methods will be inherited by all repositories
    // Additional custom methods can be added here if needed
}
