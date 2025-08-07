package com.reserch.mano.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokeSessionRequest {
    private String tokenId;
    private Boolean revokeAllSessions = false;
}
