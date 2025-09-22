package com.reserch.mano.controller.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for returning user information.
 * This object is used to send user data back to the client,
 * excluding sensitive information like the password.
 */
@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserDto(Long id, String firstName, String lastName, String email) {
    }
}
