package com.reserch.mano.service.serviceImpl;

import com.reserch.mano.controller.dto.request.SignUpRequest;
import com.reserch.mano.controller.dto.request.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Interface for the user service layer.
 * Defines the contract for user-related business logic.
 */
public interface UserService extends UserDetailsService {
    /**
     * Creates a new user.
     * @param signUpRequest The DTO containing the new user's information.
     * @return The DTO of the newly created user.
     */
    UserDto createUser(SignUpRequest signUpRequest);
}