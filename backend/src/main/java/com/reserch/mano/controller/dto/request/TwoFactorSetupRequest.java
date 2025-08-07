package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupRequest {

    @NotBlank(message = "Secret is required")
    private String secret;

    @NotBlank(message = "Verification code is required")
    private String verificationCode;
}
