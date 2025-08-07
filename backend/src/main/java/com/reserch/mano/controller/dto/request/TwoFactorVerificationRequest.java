package com.reserch.mano.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerificationRequest {

    @NotBlank(message = "Verification code is required")
    private String verificationCode;

    private Boolean trustDevice = false;
}