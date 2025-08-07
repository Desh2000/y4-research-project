package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCheckRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Permission is required")
    private String permission;

    @NotBlank(message = "Resource is required")
    private String resource;
}