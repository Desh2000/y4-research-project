package com.research.mano.controller;

import com.research.mano.dto.chat.ChatConversationDTO;
import com.research.mano.dto.chat.ChatMessageDTO;
import com.research.mano.dto.chat.ChatRequest;
import com.research.mano.dto.chat.ChatResponse;
import com.research.mano.service.ChatService;
import com.research.mano.service.Impl.CustomUserDetailsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket Controller for Real-time Chat (Component 3)
 * Handles STOMP messaging for chat functionality
 */
@Controller
public class ChatWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages
     * Client sends to: /app/chat.send
     * Response sent to: /user/queue/chat.response
     */
    @MessageMapping("/chat.send")
    @SendToUser("/queue/chat.response")
    public ChatResponse handleChatMessage(@Payload ChatRequest request, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            logger.debug("Processing WebSocket message from user: {}", userId);

            ChatResponse response = chatService.processMessage(userId, request);

            // If crisis detected, also notify monitoring system
            if (response.getCrisisDetected() != null && response.getCrisisDetected()) {
                notifyCrisisMonitoring(userId, response);
            }

            return response;
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", e.getMessage(), e);
            return ChatResponse.error("Failed to process message. Please try again.");
        }
    }

    /**
     * Handle conversation start
     * Client sends to: /app/chat.start
     * Response sent to: /user/queue/chat.started
     */
    @MessageMapping("/chat.start")
    @SendToUser("/queue/chat.started")
    public ChatConversationDTO startConversation(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            String platform = (String) payload.getOrDefault("platform", "websocket");

            Double stressLevel = payload.containsKey("stressLevel")
                    ? ((Number) payload.get("stressLevel")).doubleValue() : null;
            Double anxietyLevel = payload.containsKey("anxietyLevel")
                    ? ((Number) payload.get("anxietyLevel")).doubleValue() : null;
            Double depressionLevel = payload.containsKey("depressionLevel")
                    ? ((Number) payload.get("depressionLevel")).doubleValue() : null;

            if (stressLevel != null || anxietyLevel != null || depressionLevel != null) {
                return chatService.startConversationWithContext(userId, platform,
                        stressLevel, anxietyLevel, depressionLevel);
            }

            return chatService.startConversation(userId, platform);
        } catch (Exception e) {
            logger.error("Error starting conversation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start conversation");
        }
    }

    /**
     * Handle conversation end
     * Client sends to: /app/chat.end
     * Response sent to: /user/queue/chat.ended
     */
    @MessageMapping("/chat.end")
    @SendToUser("/queue/chat.ended")
    public ChatConversationDTO endConversation(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long conversationId = ((Number) payload.get("conversationId")).longValue();

            Double postStress = payload.containsKey("postStress")
                    ? ((Number) payload.get("postStress")).doubleValue() : null;
            Double postAnxiety = payload.containsKey("postAnxiety")
                    ? ((Number) payload.get("postAnxiety")).doubleValue() : null;
            Double postDepression = payload.containsKey("postDepression")
                    ? ((Number) payload.get("postDepression")).doubleValue() : null;

            if (postStress != null || postAnxiety != null || postDepression != null) {
                return chatService.endConversationWithAssessment(conversationId,
                        postStress, postAnxiety, postDepression);
            }

            return chatService.endConversation(conversationId);
        } catch (Exception e) {
            logger.error("Error ending conversation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to end conversation");
        }
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat.typing
     * Broadcasts to: /topic/chat.{conversationId}.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(@Payload Map<String, Object> payload, Principal principal) {
        Long conversationId = ((Number) payload.get("conversationId")).longValue();
        Boolean isTyping = (Boolean) payload.getOrDefault("isTyping", true);
        String userType = (String) payload.getOrDefault("userType", "USER");

        messagingTemplate.convertAndSend(
                "/topic/chat." + conversationId + ".typing",
                Map.of("userType", userType, "isTyping", isTyping)
        );
    }

    /**
     * Handle message feedback
     * Client sends to: /app/chat.feedback
     * Response sent to: /user/queue/chat.feedback.response
     */
    @MessageMapping("/chat.feedback")
    @SendToUser("/queue/chat.feedback.response")
    public ChatMessageDTO handleMessageFeedback(@Payload Map<String, Object> payload, Principal principal) {
        try {
            Long messageId = ((Number) payload.get("messageId")).longValue();
            Integer rating = payload.containsKey("rating")
                    ? ((Number) payload.get("rating")).intValue() : null;
            Boolean wasHelpful = (Boolean) payload.get("wasHelpful");
            String feedbackText = (String) payload.get("feedbackText");

            return chatService.addMessageFeedback(messageId, rating, wasHelpful, feedbackText);
        } catch (Exception e) {
            logger.error("Error processing feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process feedback");
        }
    }

    /**
     * Request conversation history
     * Client sends to: /app/chat.history
     * Response sent to: /user/queue/chat.history
     */
    @MessageMapping("/chat.history")
    @SendToUser("/queue/chat.history")
    public java.util.List<ChatMessageDTO> getConversationHistory(@Payload Map<String, Object> payload,
                                                                 Principal principal) {
        try {
            Long conversationId = ((Number) payload.get("conversationId")).longValue();
            Integer count = payload.containsKey("count")
                    ? ((Number) payload.get("count")).intValue() : 50;

            return chatService.getLastMessages(conversationId, count);
        } catch (Exception e) {
            logger.error("Error fetching history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch conversation history");
        }
    }

    // ==================== HELPER METHODS ====================

    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof Authentication auth) {
            Object principalObj = auth.getPrincipal();
            if (principalObj instanceof CustomUserDetailsService.UserPrincipal userPrincipal) {
                return userPrincipal.getId();
            }
        }
        throw new RuntimeException("Unable to extract user ID from principal");
    }

    /**
     * Notify healthcare professionals about crisis detection
     */
    private void notifyCrisisMonitoring(Long userId, ChatResponse response) {
        Map<String, Object> crisisAlert = Map.of(
                "userId", userId,
                "conversationId", response.getConversationId(),
                "crisisLevel", response.getCrisisLevel(),
                "indicators", response.getCrisisIndicators(),
                "timestamp", response.getTimestamp()
        );

        // Send to crisis monitoring topic
        messagingTemplate.convertAndSend("/topic/crisis.alerts", crisisAlert);
        logger.warn("Crisis alert sent for user {} - Level: {}", userId, response.getCrisisLevel());
    }
}