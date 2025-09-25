package com.research.mano.controller;


import com.research.mano.controller.request.*;
import com.research.mano.controller.responce.*;
import com.research.mano.entity.ChatConversation;
import com.research.mano.entity.User;
import com.research.mano.service.ChatConversationService;
import com.research.mano.service.UserService;
import com.research.mano.service.Impl.CustomUserDetailsService;
import com.research.mano.exception.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chat Controller
 * Handles Component 3 (Empathetic Chatbot System) REST API endpoints
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    private ChatConversationService chatConversationService;

    @Autowired
    private UserService userService;

    /**
     * POST /api/chat/message
     * Send a message to the chatbot
     */
    @PostMapping("/message")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<ChatBotResponseDTO> sendMessage(
            @Valid @RequestBody ChatMessageRequest messageRequest,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        // Generate session ID if not provided
        String sessionId = messageRequest.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = chatConversationService.generateSessionId(user);
        }

        // Create user message
        ChatConversation userMessage = chatConversationService.createMessage(
                user, sessionId, messageRequest.getMessageText(), ChatConversation.MessageType.USER_MESSAGE
        );

        // Simulate AI response (in real implementation, call ML model)
        ChatBotResponseDTO botResponse = generateBotResponse(userMessage, sessionId);

        // Create bot response record
        chatConversationService.createBotResponse(
                user, sessionId, botResponse.getResponseText(),
                calculateSentimentScore(messageRequest.getMessageText()),
                detectEmotion(messageRequest.getMessageText()),
                500L, // Simulated response time
                "chatbot-v1.0"
        );

        return ResponseEntity.ok(botResponse);
    }

    /**
     * GET /api/chat/sessions/active
     * Get active chat sessions for current user
     */
    @GetMapping("/sessions/active")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<String>> getActiveSessions(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        List<String> activeSessions = chatConversationService.getActiveSessionsForUser(user);
        return ResponseEntity.ok(activeSessions);
    }

    /**
     * GET /api/chat/conversations
     * Get conversation history for current user
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<Page<ChatConversationDTO>> getConversationHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        List<ChatConversation> conversations = chatConversationService.getConversationHistory(user);
        List<ChatConversationDTO> conversationDTOs = conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, conversationDTOs.size());
        List<ChatConversationDTO> pageContent = start < conversationDTOs.size() ?
                conversationDTOs.subList(start, end) : List.of();

        Page<ChatConversationDTO> pageResult = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, conversationDTOs.size()
        );

        return ResponseEntity.ok(pageResult);
    }

    /**
     * GET /api/chat/sessions/{sessionId}
     * Get conversation history for specific session
     */
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<ChatConversationDTO>> getSessionConversation(
            @PathVariable String sessionId,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        List<ChatConversation> conversations = chatConversationService.getConversationByUserAndSession(user, sessionId);
        List<ChatConversationDTO> conversationDTOs = conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    /**
     * POST /api/chat/sessions/{sessionId}/end
     * End chat session
     */
    @PostMapping("/sessions/{sessionId}/end")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<?> endChatSession(@PathVariable String sessionId) {
        chatConversationService.endChatSession(sessionId);
        return ResponseEntity.ok(new ApiResponse(true, "Chat session ended successfully"));
    }

    /**
     * GET /api/chat/crisis-conversations
     * Get conversations with crisis detection (Healthcare Professional/Admin only)
     */
    @GetMapping("/crisis-conversations")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatConversationDTO>> getCrisisConversations() {
        List<ChatConversation> conversations = chatConversationService.findCrisisConversations();
        List<ChatConversationDTO> conversationDTOs = conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    /**
     * GET /api/chat/intervention-triggered
     * Get conversations that triggered interventions (Healthcare Professional/Admin only)
     */
    @GetMapping("/intervention-triggered")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatConversationDTO>> getInterventionTriggeredConversations() {
        List<ChatConversation> conversations = chatConversationService.findInterventionTriggeredConversations();
        List<ChatConversationDTO> conversationDTOs = conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    /**
     * GET /api/chat/negative-sentiment
     * Get conversations with negative sentiment (Healthcare Professional/Admin only)
     */
    @GetMapping("/negative-sentiment")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatConversationDTO>> getNegativeSentimentConversations() {
        List<ChatConversation> conversations = chatConversationService.findNegativeSentimentConversations();
        List<ChatConversationDTO> conversationDTOs = conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    /**
     * GET /api/chat/recent
     * Get recent conversations for current user
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('USER') or hasRole('HEALTHCARE_PROFESSIONAL')")
    public ResponseEntity<List<ChatConversationDTO>> getRecentConversations(
            @RequestParam(defaultValue = "24") int hoursBack,
            Authentication authentication) {

        CustomUserDetailsService.UserPrincipal userPrincipal = getCurrentUser(authentication);
        User user = getUserById(userPrincipal.getId());

        List<ChatConversation> conversations = chatConversationService.getRecentConversations(user, hoursBack);
        List<ChatConversationDTO> conversationDTOs = conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    /**
     * GET /api/chat/statistics/conversation-stats
     * Get conversation statistics (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/conversation-stats")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getConversationStats(
            @RequestParam(defaultValue = "30") int daysBack) {

        List<Object[]> stats = chatConversationService.getConversationStatsByUser(daysBack);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/chat/statistics/daily-counts
     * Get daily conversation counts (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/daily-counts")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getDailyConversationCounts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        List<Object[]> counts = chatConversationService.getDailyConversationCounts(startDate);
        return ResponseEntity.ok(counts);
    }

    /**
     * GET /api/chat/statistics/sentiment-trends
     * Get sentiment trends over time (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/sentiment-trends")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getSentimentTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        List<Object[]> trends = chatConversationService.getSentimentTrendsByDate(startDate);
        return ResponseEntity.ok(trends);
    }

    /**
     * GET /api/chat/statistics/high-interaction-users
     * Get users with high interaction counts (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/high-interaction-users")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getHighInteractionUsers(
            @RequestParam(defaultValue = "30") int daysBack,
            @RequestParam(defaultValue = "50") Long interactionThreshold) {

        List<Object[]> users = chatConversationService.findHighInteractionUsers(daysBack, interactionThreshold);
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/chat/statistics/response-times
     * Get average response times by model version (Healthcare Professional/Admin only)
     */
    @GetMapping("/statistics/response-times")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getAverageResponseTimes() {
        List<Object[]> responseTimes = chatConversationService.getAverageResponseTimeByModel();
        return ResponseEntity.ok(responseTimes);
    }

    /**
     * POST /api/chat/conversations/{id}/mark-intervention
     * Mark conversation for intervention (Healthcare Professional/Admin only)
     */
    @PostMapping("/conversations/{id}/mark-intervention")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ChatConversationDTO> markForIntervention(
            @PathVariable Long id,
            @RequestParam String reason) {

        ChatConversation updatedConversation = chatConversationService.markForIntervention(id, reason);
        return ResponseEntity.ok(convertToDTO(updatedConversation));
    }

    /**
     * GET /api/chat/conversations/needing-followup
     * Get conversations needing follow-up (Healthcare Professional/Admin only)
     */
    @GetMapping("/conversations/needing-followup")
    @PreAuthorize("hasRole('HEALTHCARE_PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ChatConversationDTO>> getConversationsNeedingFollowUp() {
        List<ChatConversation> conversations = chatConversationService.findConversationsNeedingFollowUp();
        List<ChatConversationDTO> conversationDTOs = conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    // Helper methods
    private CustomUserDetailsService.UserPrincipal getCurrentUser(Authentication authentication) {
        return (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
    }

    private User getUserById(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private ChatBotResponseDTO generateBotResponse(ChatConversation userMessage, String sessionId) {
        // Simulate AI chatbot response (in real implementation, call ML model)
        ChatBotResponseDTO response = new ChatBotResponseDTO();

        String messageText = userMessage.getMessageText().toLowerCase();

        if (containsCrisisKeywords(messageText)) {
            response.setResponseText("I'm concerned about what you've shared. Your feelings are important, and you don't have to go through this alone. Would you like to talk to a mental health professional right now?");
            response.setRequiresIntervention(true);
            response.setInterventionReason("Crisis keywords detected");
            response.setSuggestedActions(new String[]{"Contact crisis hotline", "Schedule immediate consultation", "Reach out to emergency contact"});
            response.setConfidenceScore(0.95);
        } else if (messageText.contains("stress") || messageText.contains("anxious")) {
            response.setResponseText("It sounds like you're going through a stressful time. That's completely understandable. What's been the most challenging part of your day?");
            response.setRequiresIntervention(false);
            response.setSuggestedActions(new String[]{"Try breathing exercises", "Take a short break", "Consider mindfulness techniques"});
            response.setConfidenceScore(0.85);
        } else if (messageText.contains("sad") || messageText.contains("down")) {
            response.setResponseText("I hear that you're feeling down. It takes courage to share that. Can you tell me more about what's been affecting your mood lately?");
            response.setRequiresIntervention(false);
            response.setSuggestedActions(new String[]{"Engage in self-care activities", "Connect with friends", "Consider professional support"});
            response.setConfidenceScore(0.80);
        } else {
            response.setResponseText("Thank you for sharing that with me. I'm here to listen and support you. How are you feeling right now?");
            response.setRequiresIntervention(false);
            response.setSuggestedActions(new String[]{"Continue conversation", "Explore feelings", "Take assessment"});
            response.setConfidenceScore(0.75);
        }

        response.setNextSteps("Continue the conversation or ask for professional help if needed.");
        return response;
    }

    private boolean containsCrisisKeywords(String text) {
        String[] crisisKeywords = {"suicide", "kill myself", "end it all", "can't go on", "want to die", "hurt myself", "self-harm", "hopeless", "worthless"};
        for (String keyword : crisisKeywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Double calculateSentimentScore(String text) {
        // Simulate sentiment analysis (in real implementation, call ML model)
        String lowerText = text.toLowerCase();
        double score = 0.0;

        // Positive keywords
        if (lowerText.contains("happy") || lowerText.contains("good") || lowerText.contains("great")) {
            score += 0.3;
        }
        if (lowerText.contains("love") || lowerText.contains("joy") || lowerText.contains("excited")) {
            score += 0.4;
        }

        // Negative keywords
        if (lowerText.contains("sad") || lowerText.contains("bad") || lowerText.contains("terrible")) {
            score -= 0.3;
        }
        if (lowerText.contains("hate") || lowerText.contains("awful") || lowerText.contains("hopeless")) {
            score -= 0.5;
        }

        return Math.max(-1.0, Math.min(1.0, score));
    }

    private String detectEmotion(String text) {
        // Simulate emotion detection (in real implementation, call ML model)
        String lowerText = text.toLowerCase();

        if (lowerText.contains("angry") || lowerText.contains("mad") || lowerText.contains("frustrated")) {
            return "anger";
        } else if (lowerText.contains("sad") || lowerText.contains("depressed") || lowerText.contains("down")) {
            return "sadness";
        } else if (lowerText.contains("anxious") || lowerText.contains("worried") || lowerText.contains("nervous")) {
            return "anxiety";
        } else if (lowerText.contains("happy") || lowerText.contains("joy") || lowerText.contains("excited")) {
            return "happiness";
        } else {
            return "neutral";
        }
    }

    private ChatConversationDTO convertToDTO(ChatConversation conversation) {
        ChatConversationDTO dto = new ChatConversationDTO();
        dto.setId(conversation.getId());
        dto.setUserId(conversation.getUser().getId());
        dto.setUsername(conversation.getUser().getUsername());
        dto.setSessionId(conversation.getSessionId());
        dto.setMessageText(conversation.getMessageText());
        dto.setMessageType(conversation.getMessageType().name());
        dto.setSentimentScore(conversation.getSentimentScore());
        dto.setEmotionDetected(conversation.getEmotionDetected());
        dto.setCrisisKeywordsDetected(conversation.getCrisisKeywordsDetected());
        dto.setInterventionTriggered(conversation.getInterventionTriggered());
        dto.setResponseTimeMs(conversation.getResponseTimeMs());
        dto.setModelVersion(conversation.getModelVersion());
        dto.setContextData(conversation.getContextData());
        dto.setCreatedAt(conversation.getCreatedAt());

        return dto;
    }

    /**
     * Generic API Response class
     */
    public static class ApiResponse {
        private Boolean success;
        private String message;

        public ApiResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}