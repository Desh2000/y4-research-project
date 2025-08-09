package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    private LocalDate dateOfBirth;
    private String gender;

    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String phoneNumber;

    private String profilePictureUrl;
    private String mentalHealthGoals;
    private String emergencyContact;
    private String timezone;
}