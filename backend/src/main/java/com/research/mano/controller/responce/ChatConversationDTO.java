package com.research.mano.controller.responce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatConversationDTO {
    private Long id;
    private Long userId;
    private String username;
    private String sessionId;
    private String messageText;
    private String messageType;
    private Double sentimentScore;
    private String emotionDetected;
    private Boolean crisisKeywordsDetected;
    private Boolean interventionTriggered;
    private Long responseTimeMs;
    private String modelVersion;
    private String contextData;
    private LocalDateTime createdAt;
}