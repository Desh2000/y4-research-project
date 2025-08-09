package com.reserch.mano.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String content;
    private String messageType;
    private Double sentimentScore;
    private Boolean interventionTriggered;
    private LocalDateTime createdAt;
    private Long sessionId;
}