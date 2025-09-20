package com.reserch.mano.service.serviceImpl.impl;

import com.reserch.mano.controller.dto.request.SignUpRequest;
import com.reserch.mano.controller.dto.request.UserDto;
import com.reserch.mano.model.User;
import com.reserch.mano.repository.UserRepository;
import com.reserch.mano.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Implementation of the UserService interface.
 * Contains the business logic for user-related operations.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserServiceImpl with the required dependencies.
     * @param userRepository The repository for user data access.
     * @param passwordEncoder The encoder for hashing passwords.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user and saves them to the database.
     * The user's password is hashed before being stored.
     *
     * @param signUpRequest The DTO containing the new user's information.
     * @return A DTO representing the newly created user.
     */
    @Override
    public UserDto createUser(SignUpRequest signUpRequest) {
        // Create a new User entity
        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // Save the user to the database
        User createdUser = userRepository.save(user);

        // Convert the User entity to a UserDto for the response
        UserDto userDto = new UserDto();
        userDto.setId(createdUser.getId());
        userDto.setFirstName(createdUser.getFirstName());
        userDto.setLastName(createdUser.getLastName());
        userDto.setEmail(createdUser.getEmail());

        return userDto;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}