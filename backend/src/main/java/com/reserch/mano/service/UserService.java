package com.reserch.mano.service;

import com.reserch.mano.controller.dto.request.UpdateUserRequest;
import com.reserch.mano.controller.dto.response.UserResponse;
import com.reserch.mano.model.User;
import com.reserch.mano.securuty.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse getUserByUsername(String username);

    UserResponse getCurrentUser(UserPrincipal userPrincipal);

    UserResponse updateUser(UserPrincipal userPrincipal, UpdateUserRequest updateUserRequest);

    void deactivateUser(Long userId);

    void activateUser(Long userId);

    void deleteUser(Long userId);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> searchUsers(String keyword, Pageable pageable);

    List<UserResponse> getUsersByRole(String roleName);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void updateLastLogin(Long userId, LocalDateTime lastLogin);

    long getUserCount();

    long getActiveUserCount();

    List<UserResponse> getRecentUsers(int days);

    Optional<User> findEntityById(Long id);

    Optional<User> findEntityByEmail(String email);
}
