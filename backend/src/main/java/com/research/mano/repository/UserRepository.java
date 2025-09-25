package com.research.mano.repository;

import com.research.mano.entity.User;
import com.research.mano.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * User Repository Interface
 * Handles CRUD operations for User entity with authentication support
 */
@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email verification token
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find user by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find user by refresh token
     */
    Optional<User> findByRefreshToken(String refreshToken);

    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find users by role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.isActive = true")
    List<User> findByRole(@Param("role") Role role);

    /**
     * Find users by role name
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true")
    List<User> findByRoleName(@Param("roleName") Role.RoleName roleName);

    /**
     * Find users with mental health consent
     */
    List<User> findByPrivacyConsentTrueAndIsActiveTrue();

    /**
     * Find users with data sharing consent
     */
    List<User> findByDataSharingConsentTrueAndIsActiveTrue();

    /**
     * Find users by mental health status
     */
    List<User> findByMentalHealthStatusAndIsActiveTrue(User.MentalHealthStatus status);

    /**
     * Find users with high risk mental health status
     */
    @Query("SELECT u FROM User u WHERE u.mentalHealthStatus IN ('HIGH_CONCERN', 'CRISIS') AND u.isActive = true")
    List<User> findHighRiskUsers();

    /**
     * Find users who haven't logged in for specific days
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :cutoffDate AND u.isActive = true")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find users needing email verification
     */
    List<User> findByIsEmailVerifiedFalseAndIsActiveTrue();

    /**
     * Find users with expired password reset tokens
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetExpires < :now AND u.passwordResetToken IS NOT NULL")
    List<User> findUsersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    /**
     * Find users with expired refresh tokens
     */
    @Query("SELECT u FROM User u WHERE u.refreshTokenExpires < :now AND u.refreshToken IS NOT NULL")
    List<User> findUsersWithExpiredRefreshTokens(@Param("now") LocalDateTime now);

    /**
     * Update last login time
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Update email verification status
     */
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = true, u.emailVerificationToken = null WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") Long userId);

    /**
     * Clear password reset token
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetExpires = null WHERE u.id = :userId")
    void clearPasswordResetToken(@Param("userId") Long userId);

    /**
     * Update refresh token
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :token, u.refreshTokenExpires = :expires WHERE u.id = :userId")
    void updateRefreshToken(@Param("userId") Long userId,
                            @Param("token") String token,
                            @Param("expires") LocalDateTime expires);

    /**
     * Clear refresh token
     */
    @Modifying
    @Query("UPDATE User u SET u.refreshToken = null, u.refreshTokenExpires = null WHERE u.id = :userId")
    void clearRefreshToken(@Param("userId") Long userId);

    /**
     * Count users by mental health status
     */
    @Query("SELECT u.mentalHealthStatus, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.mentalHealthStatus")
    List<Object[]> countUsersByMentalHealthStatus();

    /**
     * Find users for Component 1 (synthetic data generation) - with consent
     */
    @Query("SELECT u FROM User u WHERE u.privacyConsent = true AND u.dataSharingConsent = true AND u.isActive = true")
    List<User> findUsersForSyntheticDataGeneration();

    /**
     * Search users by name or username
     */
    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND u.isActive = true")
    List<User> searchUsersByName(@Param("searchTerm") String searchTerm);
}