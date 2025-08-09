package com.reserch.mano.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private String profilePictureUrl;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Set<String> roles;
}

