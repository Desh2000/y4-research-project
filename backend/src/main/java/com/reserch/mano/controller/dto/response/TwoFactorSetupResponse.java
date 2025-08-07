package com.reserch.mano.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TwoFactorSetupResponse {
    private String qrCodeUrl;
    private String secret;
    private String[] backupCodes;
    private Boolean isEnabled;
}
