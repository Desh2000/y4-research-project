package com.reserch.mano.controller.dto;

import com.reserch.mano.controller.dto.request.SignInRequest;
import com.reserch.mano.controller.dto.request.SignUpRequest;
import com.reserch.mano.controller.dto.request.UserDto;
import com.reserch.mano.controller.dto.responce.JwtAuthenticationResponse;
import com.reserch.mano.service.serviceImpl.AuthenticationService;
import com.reserch.mano.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling authentication-related requests, such as user sign-up.
 */
@RestController // Marks this class as a REST controller, which combines @Controller and @ResponseBody
@RequestMapping("/api/auth") // Maps HTTP requests to /api/auth to this controller
public class AuthController {

    private final UserService userService;
    private AuthenticationService authenticationService;

    // Injects the UserService dependency
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    /**
     * Handles the user sign-up request.
     * Maps to the POST /api/auth/signup endpoint.
     *
     * @param signUpRequest The request body containing the user's details for registration.
     * @return A ResponseEntity containing the created user's DTO and an HTTP status of 201 (Created).
     */
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signupUser(@RequestBody SignUpRequest signUpRequest) {
        // Call the user service to create a new user
        UserDto createdUser = userService.createUser(signUpRequest);
        // Return the created user DTO with a 201 Created status
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }
}