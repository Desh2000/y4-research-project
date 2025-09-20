package com.reserch.mano.service.serviceImpl;

import com.reserch.mano.controller.dto.request.SignInRequest;
import com.reserch.mano.controller.dto.request.SignUpRequest;
import com.reserch.mano.controller.dto.request.UserDto;
import com.reserch.mano.controller.dto.responce.JwtAuthenticationResponse;

public interface AuthenticationService {
    /**
     * Registers a new user in the system.
     * @param signUpRequest The request object containing user details for registration.
     * @return A DTO of the created user.
     */
    UserDto signup(SignUpRequest signUpRequest);

    /**
     * Authenticates a user and returns a JWT.
     * @param signInRequest The request object containing user credentials for signing in.
     * @return A response object containing the JWT.
     */
    JwtAuthenticationResponse signin(SignInRequest signInRequest);
}
