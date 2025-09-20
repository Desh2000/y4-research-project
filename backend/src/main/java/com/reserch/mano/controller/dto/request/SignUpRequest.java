package com.reserch.mano.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for handling user registration requests.
 * This object carries the data from the client to the server for creating a new user.
 */
@Data
@AllArgsConstructor
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
