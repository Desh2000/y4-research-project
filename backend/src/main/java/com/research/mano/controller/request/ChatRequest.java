package com.research.mano.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Request DTO for sending chat messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * Conversation ID (null for new conversation)
     */
    private Long conversationId;

    /**
     * Message content
     */
    @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String message;

    /**
     * Additional context (optional)
     */
    private Map<String, Object> context;

    /**
     * Current mood/state indicators (optional)
     */
    private Double currentStressLevel;
    private Double currentAnxietyLevel;
    private Double currentMoodScore;

    /**
     * Client platform
     */
    private String platform; // web, android, ios

    /**
     * Whether to include analysis in response
     */
    private Boolean includeAnalysis = false;
}