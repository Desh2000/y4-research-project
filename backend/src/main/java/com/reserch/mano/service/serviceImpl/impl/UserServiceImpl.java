package com.reserch.mano.service.serviceImpl.impl;

import com.reserch.mano.controller.dto.request.SignUpRequest;
import com.reserch.mano.controller.dto.request.UserDto;
import com.reserch.mano.exception.ResourceNotFoundException;
import com.reserch.mano.model.User;
import com.reserch.mano.repository.UserRepository;
import com.reserch.mano.service.serviceImpl.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Implementation of the UserService interface.
 * Contains the business logic for user-related operations.
 */
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

//    @Autowired
//    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public UserDto createUser(SignUpRequest signUpRequest) {
//        // Create a new User entity
//        User user = new User();
//        user.setFirstName(signUpRequest.getFirstName());
//        user.setLastName(signUpRequest.getLastName());
//        user.setEmail(signUpRequest.getEmail());
//        // Encode the password before saving
//        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
//
//        // Save the user to the database
//        User createdUser = userRepository.save(user);
//
//        // Convert the User entity to a UserDto for the response
//        UserDto userDto = new UserDto();
//        userDto.setId(createdUser.getId());
//        userDto.setFirstName(createdUser.getFirstName());
//        userDto.setLastName(createdUser.getLastName());
//        userDto.setEmail(createdUser.getEmail());
//
//        return userDto;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return null;
//    }

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
            }
        };
    }

    @Override
    public UserDto createUser(SignUpRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use.");
        }

        User user = new User();
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        User savedUser = userRepository.save(user);

        return new UserDto(savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(), savedUser.getEmail());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}