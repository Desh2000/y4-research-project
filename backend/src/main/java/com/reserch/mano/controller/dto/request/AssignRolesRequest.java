package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolesRequest {

    @NotBlank(message = "Username is required")
    private String username;

    private Set<String> roleNames;
    private String reason;
}
