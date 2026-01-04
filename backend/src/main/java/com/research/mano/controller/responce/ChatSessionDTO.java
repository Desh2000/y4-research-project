package com.research.mano.controller.responce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionDTO {
    private String sessionId;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime lastActivity;
    private Integer messageCount;
    private Double averageSentiment;
    private Boolean crisisDetected;
    private String status; // ACTIVE, ENDED, INTERVENTION_REQUIRED
}
