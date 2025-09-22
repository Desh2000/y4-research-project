package com.reserch.mano.service.serviceImpl;

import com.reserch.mano.controller.dto.request.SignUpRequest;
import com.reserch.mano.controller.dto.request.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserService extends UserDetailsService {

    UserDetailsService userDetailsService();

    UserDto createUser(SignUpRequest signUpRequest);
}