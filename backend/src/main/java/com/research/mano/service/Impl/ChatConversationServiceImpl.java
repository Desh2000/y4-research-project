package com.research.mano.service.Impl;

import com.research.mano.entity.ChatConversation;
import com.research.mano.entity.User;
import com.research.mano.repository.ChatConversationRepository;
import com.research.mano.service.ChatConversationService;
import com.research.mano.service.SystemAlertService;
import com.research.mano.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Chat Conversation Service Implementation
 * Handles Component 3 (Empathetic Chatbot System) business logic
 */
@Service
@Transactional
public class ChatConversationServiceImpl implements ChatConversationService {

    @Autowired
    private ChatConversationRepository chatConversationRepository;

    @Autowired
    private SystemAlertService systemAlertService;

    @Autowired
    private UserProfileService userProfileService;

    // Crisis keywords for detection
    private static final String[] CRISIS_KEYWORDS = {
            "suicide", "kill myself", "end it all", "can't go on", "want to die",
            "hurt myself", "self-harm", "hopeless", "worthless", "give up"
    };

    private static final Pattern CRISIS_PATTERN = Pattern.compile(
            String.join("|", CRISIS_KEYWORDS), Pattern.CASE_INSENSITIVE
    );

    @Override
    public ChatConversation save(ChatConversation conversation) {
        return chatConversationRepository.save(conversation);
    }

    @Override
    public List<ChatConversation> saveAll(List<ChatConversation> conversations) {
        return chatConversationRepository.saveAll(conversations);
    }

    @Override
    public Optional<ChatConversation> findById(Long id) {
        return chatConversationRepository.findById(id);
    }

    @Override
    public List<ChatConversation> findAll() {
        return chatConversationRepository.findAll();
    }

    @Override
    public Page<ChatConversation> findAll(Pageable pageable) {
        return chatConversationRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return chatConversationRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        chatConversationRepository.deleteById(id);
    }

    @Override
    public void delete(ChatConversation conversation) {
        chatConversationRepository.delete(conversation);
    }

    @Override
    public long count() {
        return chatConversationRepository.count();
    }

    @Override
    public ChatConversation createMessage(User user, String sessionId, String messageText,
                                          ChatConversation.MessageType messageType) {
        ChatConversation conversation = new ChatConversation(user, sessionId, messageText, messageType);

        // Process crisis detection for user messages
        if (messageType == ChatConversation.MessageType.USER_MESSAGE) {
            processCrisisDetection(conversation);

            // Update chatbot interaction count
            updateChatbotInteractionCount(user);
        }

        return chatConversationRepository.save(conversation);
    }

    @Override
    public ChatConversation createBotResponse(User user, String sessionId, String responseText,
                                              Double sentimentScore, String emotionDetected,
                                              Long responseTimeMs, String modelVersion) {
        ChatConversation conversation = new ChatConversation(user, sessionId, responseText,
                ChatConversation.MessageType.BOT_RESPONSE);
        conversation.setSentimentScore(sentimentScore);
        conversation.setEmotionDetected(emotionDetected);
        conversation.setResponseTimeMs(responseTimeMs);
        conversation.setModelVersion(modelVersion);

        return chatConversationRepository.save(conversation);
    }

    @Override
    public List<ChatConversation> getConversationHistory(User user) {
        return chatConversationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<ChatConversation> getConversationBySession(String sessionId) {
        return chatConversationRepository.findBySessionIdOrderByCreatedAt(sessionId);
    }

    @Override
    public List<ChatConversation> getConversationByUserAndSession(User user, String sessionId) {
        return chatConversationRepository.findByUserAndSessionIdOrderByCreatedAt(user, sessionId);
    }

    @Override
    public List<ChatConversation> findCrisisConversations() {
        return chatConversationRepository.findByCrisisKeywordsDetectedTrue();
    }

    @Override
    public List<ChatConversation> findInterventionTriggeredConversations() {
        return chatConversationRepository.findByInterventionTriggeredTrue();
    }

    @Override
    public List<ChatConversation> findBySentimentRange(Double minScore, Double maxScore) {
        return chatConversationRepository.findBySentimentScoreRange(minScore, maxScore);
    }

    @Override
    public List<ChatConversation> findNegativeSentimentConversations() {
        return chatConversationRepository.findNegativeSentimentConversations();
    }

    @Override
    public List<ChatConversation> getRecentConversations(User user, int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return chatConversationRepository.findRecentConversationsByUser(user, since);
    }

    @Override
    public List<Object[]> getConversationStatsByUser(int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return chatConversationRepository.getConversationStatsByUser(since);
    }

    @Override
    public List<Object[]> getDailyConversationCounts(LocalDateTime startDate) {
        return chatConversationRepository.getDailyConversationCounts(startDate);
    }

    @Override
    public List<ChatConversation> findByEmotionDetected(String emotion) {
        return chatConversationRepository.findByEmotionDetected(emotion);
    }

    @Override
    public List<Object[]> getAverageResponseTimeByModel() {
        return chatConversationRepository.getAverageResponseTimeByModel();
    }

    @Override
    public List<ChatConversation> findSlowResponseConversations(Long thresholdMs) {
        return chatConversationRepository.findSlowResponseConversations(thresholdMs);
    }

    @Override
    public List<Object[]> countConversationsByModelVersion() {
        return chatConversationRepository.countConversationsByModelVersion();
    }

    @Override
    public List<ChatConversation> findConversationsForMLTraining(int daysBack) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
        return chatConversationRepository.findConversationsForMLTraining(cutoffDate);
    }

    @Override
    public List<Object[]> getSentimentTrendsByDate(LocalDateTime startDate) {
        return chatConversationRepository.getSentimentTrendsByDate(startDate);
    }

    @Override
    public List<Object[]> findHighInteractionUsers(int daysBack, Long interactionThreshold) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return chatConversationRepository.findHighInteractionUsers(since, interactionThreshold);
    }

    @Override
    public ChatConversation processCrisisDetection(Long conversationId) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        return processCrisisDetection(conversation);
    }

    private ChatConversation processCrisisDetection(ChatConversation conversation) {
        boolean crisisDetected = detectCrisisSituation(conversation);

        conversation.setCrisisKeywordsDetected(crisisDetected);

        if (crisisDetected) {
            triggerCrisisIntervention(conversation);
        }

        return conversation;
    }

    @Override
    public ChatConversation updateSentimentAnalysis(Long conversationId, Double sentimentScore, String emotions) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setSentimentScore(sentimentScore);
        conversation.setEmotionDetected(emotions);

        return chatConversationRepository.save(conversation);
    }

    @Override
    public String generateSessionId(User user) {
        return "SESSION-" + user.getId() + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public void endChatSession(String sessionId) {
        List<ChatConversation> sessionConversations = getConversationBySession(sessionId);

        // Analyze session for patterns
        if (!sessionConversations.isEmpty()) {
            User user = sessionConversations.get(0).getUser();
            analyzeConversationPatterns(user);
        }
    }

    @Override
    public List<String> getActiveSessionsForUser(User user) {
        LocalDateTime recentCutoff = LocalDateTime.now().minusHours(2); // Sessions active in last 2 hours

        return chatConversationRepository.findRecentConversationsByUser(user, recentCutoff)
                .stream()
                .map(ChatConversation::getSessionId)
                .distinct()
                .toList();
    }

    @Override
    public void analyzeConversationPatterns(User user) {
        List<ChatConversation> recentConversations = getRecentConversations(user, 24);

        if (recentConversations.isEmpty()) return;

        // Calculate average sentiment
        double avgSentiment = recentConversations.stream()
                .filter(c -> c.getSentimentScore() != null)
                .mapToDouble(ChatConversation::getSentimentScore)
                .average()
                .orElse(0.0);

        // Count crisis indicators
        long crisisCount = recentConversations.stream()
                .filter(c -> Boolean.TRUE.equals(c.getCrisisKeywordsDetected()))
                .count();

        // Generate alerts if patterns indicate risk
        if (avgSentiment < -0.5 || crisisCount > 2) {
            systemAlertService.createChatCrisisAlert(user,
                    String.format("Concerning chat patterns detected: avg sentiment=%.2f, crisis indicators=%d",
                            avgSentiment, crisisCount),
                    recentConversations.get(0).getSessionId()
            );
        }
    }

    @Override
    public void updateChatbotInteractionCount(User user) {
        userProfileService.incrementChatbotInteraction(user.getId());
    }

    @Override
    public boolean detectCrisisSituation(ChatConversation conversation) {
        if (conversation.getMessageText() == null) return false;

        String message = conversation.getMessageText().toLowerCase();

        // Check for crisis keywords
        boolean keywordMatch = CRISIS_PATTERN.matcher(message).find();

        // Check sentiment score
        boolean negativeSentiment = conversation.getSentimentScore() != null &&
                conversation.getSentimentScore() < -0.8;

        return keywordMatch || negativeSentiment;
    }

    @Override
    public void triggerCrisisIntervention(ChatConversation conversation) {
        conversation.setInterventionTriggered(true);

        // Create crisis alert
        systemAlertService.createChatCrisisAlert(
                conversation.getUser(),
                conversation.getMessageText(),
                conversation.getSessionId()
        );

        // Set high-risk flag in user profile
        userProfileService.setHighRiskAlert(conversation.getUser().getId(), true);
        userProfileService.setInterventionRequired(conversation.getUser().getId(), true);
    }

    @Override
    public String getConversationContext(User user, String sessionId, int messageLimit) {
        List<ChatConversation> conversations = getConversationByUserAndSession(user, sessionId);

        return conversations.stream()
                .limit(messageLimit)
                .map(c -> c.getMessageType().name() + ": " + c.getMessageText())
                .reduce("", (a, b) -> a + "\n" + b);
    }

    @Override
    public List<ChatConversation> findConversationsNeedingFollowUp() {
        List<ChatConversation> crisisConversations = findCrisisConversations();
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        return crisisConversations.stream()
                .filter(c -> c.getCreatedAt().isAfter(oneDayAgo))
                .filter(c -> !Boolean.TRUE.equals(c.getInterventionTriggered()))
                .toList();
    }

    @Override
    public ChatConversation markForIntervention(Long conversationId, String reason) {
        ChatConversation conversation = chatConversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setInterventionTriggered(true);

        // Add context to conversation
        String existingContext = conversation.getContextData() != null ? conversation.getContextData() : "{}";
        String updatedContext = existingContext.replace("}",
                String.format(",\"interventionReason\":\"%s\",\"interventionDate\":\"%s\"}",
                        reason, LocalDateTime.now().toString()));
        conversation.setContextData(updatedContext);

        return chatConversationRepository.save(conversation);
    }
}
