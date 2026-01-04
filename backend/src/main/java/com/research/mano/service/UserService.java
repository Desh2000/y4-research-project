package com.research.mano.service;

import com.research.mano.entity.User;
import com.research.mano.entity.Role;
import com.research.mano.entity.UserProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Service Interface
 * Business logic for user management and authentication
 */
public interface UserService extends BaseService<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find the user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find the user by username or email
     */
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);

    /**
     * Check if a username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Create a new user with a default role
     */
    User createUser(String username, String email, String password, String firstName, String lastName);

    /**
     * Create user with specific roles
     */
    User createUser(String username, String email, String password, String firstName, String lastName, List<Role> roles);

    /**
     * Update user profile
     */
    User updateUser(Long userId, User updatedUser);

    /**
     * Change user password
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Reset password
     */
    void resetPassword(String email);

    /**
     * Verify email
     */
    boolean verifyEmail(String token);

    /**
     * Update last login time
     */
    void updateLastLogin(Long userId);

    /**
     * Add role to user
     */
    void addRoleToUser(Long userId, Role role);

    /**
     * Remove role from user
     */
    void removeRoleFromUser(Long userId, Role role);

    /**
     * Find users by role
     */
    List<User> findUsersByRole(Role.RoleName roleName);

    /**
     * Find high-risk users
     */
    List<User> findHighRiskUsers();

    /**
     * Find inactive users
     */
    List<User> findInactiveUsers(int daysSinceLastLogin);

    /**
     * Deactivate user
     */
    void deactivateUser(Long userId, String reason);

    /**
     * Activate user
     */
    void activateUser(Long userId);

    /**
     * Search users by name
     */
    List<User> searchUsersByName(String searchTerm);

    /**
     * Get users for synthetic data generation (with consent)
     */
    List<User> getUsersForSyntheticDataGeneration();

    /**
     * Update refresh token
     */
    void updateRefreshToken(Long userId, String refreshToken, LocalDateTime expiresAt);

    /**
     * Clear refresh token
     */
    void clearRefreshToken(Long userId);

    /**
     * Find user by refresh token
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * Clean expired tokens
     */
    void cleanExpiredTokens();
}