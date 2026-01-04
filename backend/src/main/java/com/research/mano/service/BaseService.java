package com.research.mano.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Base Service Interface
 * Provides common service methods for all entities
 * @param <T> Entity type
 * @param <ID> ID type
 */
public interface BaseService<T, ID> {

    /**
     * Save entity
     */
    T save(T entity);

    /**
     * Save all entities
     */
    List<T> saveAll(List<T> entities);

    /**
     * Find entity by ID
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities
     */
    List<T> findAll();

    /**
     * Find all entities with pagination
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Check if an entity exists by ID
     */
    boolean existsById(ID id);

    /**
     * Delete entity by ID
     */
    void deleteById(ID id);

    /**
     * Delete entity
     */
    void delete(T entity);

    /**
     * Count all entities
     */
    long count();
}
