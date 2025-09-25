package com.research.mano.service.Impl;

import com.research.mano.entity.User;
import com.research.mano.entity.Role;
import com.research.mano.entity.UserProfile;
import com.research.mano.repository.UserRepository;
import com.research.mano.repository.RoleRepository;
import com.research.mano.service.UserService;
import com.research.mano.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * User Service Implementation
 * Handles user management and authentication business logic
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> saveAll(List<User> users) {
        return userRepository.saveAll(users);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User createUser(String username, String email, String password, String firstName, String lastName) {
        // Get default user role
        Role defaultRole = roleRepository.findDefaultUserRole()
                .orElseThrow(() -> new RuntimeException("Default user role not found"));

        return createUser(username, email, password, firstName, lastName, Arrays.asList(defaultRole));
    }

    @Override
    public User createUser(String username, String email, String password, String firstName, String lastName, List<Role> roles) {
        // Check if username or email already exists
        if (existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setRoles(new HashSet<>(roles));

        // Save user
        User savedUser = userRepository.save(user);

        // Create user profile
        userProfileService.createProfile(savedUser);

        return savedUser;
    }

    @Override
    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update allowed fields
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getPhone() != null) {
            existingUser.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
        }
        if (updatedUser.getGender() != null) {
            existingUser.setGender(updatedUser.getGender());
        }
        if (updatedUser.getProfileImageUrl() != null) {
            existingUser.setProfileImageUrl(updatedUser.getProfileImageUrl());
        }

        return userRepository.save(existingUser);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Override
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // TODO: Send reset email
        // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Override
    public boolean verifyEmail(String token) {
        Optional<User> userOptional = userRepository.findByEmailVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsEmailVerified(true);
            user.setEmailVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
    }

    @Override
    public void addRoleToUser(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    public void removeRoleFromUser(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.removeRole(role);
        userRepository.save(user);
    }

    @Override
    public List<User> findUsersByRole(Role.RoleName roleName) {
        return userRepository.findByRoleName(roleName);
    }

    @Override
    public List<User> findHighRiskUsers() {
        return userRepository.findHighRiskUsers();
    }

    @Override
    public List<User> findInactiveUsers(int daysSinceLastLogin) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceLastLogin);
        return userRepository.findInactiveUsers(cutoffDate);
    }

    @Override
    public void deactivateUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);
        // TODO: Log deactivation reason
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    public List<User> searchUsersByName(String searchTerm) {
        return userRepository.searchUsersByName(searchTerm);
    }

    @Override
    public List<User> getUsersForSyntheticDataGeneration() {
        return userRepository.findUsersForSyntheticDataGeneration();
    }

    @Override
    public void updateRefreshToken(Long userId, String refreshToken, LocalDateTime expiresAt) {
        userRepository.updateRefreshToken(userId, refreshToken, expiresAt);
    }

    @Override
    public void clearRefreshToken(Long userId) {
        userRepository.clearRefreshToken(userId);
    }

    @Override
    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    @Override
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        // Clean expired password reset tokens
        List<User> usersWithExpiredPasswordTokens = userRepository.findUsersWithExpiredPasswordResetTokens(now);
        for (User user : usersWithExpiredPasswordTokens) {
            user.setPasswordResetToken(null);
            user.setPasswordResetExpires(null);
            userRepository.save(user);
        }

        // Clean expired refresh tokens
        List<User> usersWithExpiredRefreshTokens = userRepository.findUsersWithExpiredRefreshTokens(now);
        for (User user : usersWithExpiredRefreshTokens) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpires(null);
            userRepository.save(user);
        }
    }
}