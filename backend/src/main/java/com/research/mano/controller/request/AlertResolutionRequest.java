package com.research.mano.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertResolutionRequest {
    private String resolvedBy;
    private String resolutionNotes;
    private String actionTaken;
}
