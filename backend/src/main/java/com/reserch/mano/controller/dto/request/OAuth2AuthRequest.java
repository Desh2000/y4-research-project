package com.reserch.mano.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public  class OAuth2AuthRequest {

    @NotBlank(message = "Provider is required")
    private String provider; // google, facebook, github, etc.

    @NotBlank(message = "Authorization code is required")
    private String code;

    private String redirectUri;
    private String state;
}
