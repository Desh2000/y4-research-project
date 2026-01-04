package com.research.mano.controller.responce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ChatBotResponseDTO {
    private String responseText;
    private Double confidenceScore;
    private String[] suggestedActions;
    private Boolean requiresIntervention;
    private String interventionReason;
    private String nextSteps;
}
