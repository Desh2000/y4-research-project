package com.reserch.mano.repository;

import com.reserch.mano.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * This interface extends JpaRepository, which provides CRUD operations for the User entity.
 */
@Repository // Declares this interface as a Spring repository bean
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Spring Data JPA will automatically generate the implementation for this method
     * based on the method name.
     *
     * @param email The email of the user to find.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findByEmail(String email);
}