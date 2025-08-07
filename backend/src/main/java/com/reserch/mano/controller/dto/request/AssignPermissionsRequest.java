package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionsRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;

    private Set<String> permissions; // Format: "PERMISSION_NAME_RESOURCE"
    private String reason;
}
