package com.research.mano.repository;


import com.research.mano.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Role Repository Interface
 * Handles CRUD operations for Role entity
 */
@Repository
public interface RoleRepository extends BaseRepository<Role, Long> {

    /**
     * Find a role by name
     */
    Optional<Role> findByName(Role.RoleName name);

    /**
     * Check if a role exists by name
     */
    boolean existsByName(Role.RoleName name);

    /**
     * Find all active roles
     */
    List<Role> findByIsActiveTrue();

    /**
     * Find roles by description containing a keyword
     */
    List<Role> findByDescriptionContainingIgnoreCase(String keyword);

    /**
     * Get default user role
     */
    @Query("SELECT r FROM Role r WHERE r.name = 'ROLE_USER' AND r.isActive = true")
    Optional<Role> findDefaultUserRole();

    /**
     * Get all therapist and admin roles
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('ROLE_THERAPIST', 'ROLE_ADMIN') AND r.isActive = true")
    List<Role> findProfessionalRoles();

    /**
     * Get roles for mental health professionals
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('ROLE_THERAPIST', 'ROLE_RESEARCHER', 'ROLE_MODERATOR') AND r.isActive = true")
    List<Role> findMentalHealthProfessionalRoles();
}
