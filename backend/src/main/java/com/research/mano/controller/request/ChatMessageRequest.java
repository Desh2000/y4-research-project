package com.research.mano.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Chat and Conversation DTOs for Component 3 (Empathetic Chatbot)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
// Chat Message Request
public class ChatMessageRequest {
    @NotBlank(message = "Message text is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String messageText;

    private String sessionId;
}