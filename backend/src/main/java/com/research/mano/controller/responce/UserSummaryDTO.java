package com.research.mano.controller.responce;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private boolean isEmailVerified;
    private String[] roles;
    private String mentalHealthStatus;

    public UserSummaryDTO(Long id, @NotBlank @Size(max = 50) String username, @NotBlank @Size(max = 100) @Email String email, @Size(max = 50) String firstName, @Size(max = 50) String lastName) {
    }
}
