package com.research.mano.service.Impl;

import com.research.mano.dto.chat.ChatConversationDTO;
import com.research.mano.dto.chat.ChatMessageDTO;
import com.research.mano.dto.chat.ChatRequest;
import com.research.mano.dto.chat.ChatResponse;
import com.research.mano.entity.ChatConversation;
import com.research.mano.entity.ChatConversation.ConversationStatus;
import com.research.mano.entity.ChatMessage;
import com.research.mano.entity.ChatMessage.CrisisLevel;
import com.research.mano.entity.ChatMessage.SenderType;
import com.research.mano.entity.ChatMessage.SentimentLabel;
import com.research.mano.entity.User;
import com.research.mano.repository.ChatConversationRepository;
import com.research.mano.repository.ChatMessageRepository;
import com.research.mano.repository.UserRepository;
import com.research.mano.service.ChatService;
import com.research.mano.service.ml.MLServiceClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Chat Service Implementation for Component 3
 * Handles empathetic conversation with crisis detection
 */
@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    @Autowired
    private ChatConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MLServiceClient mlServiceClient;

    // Crisis detection patterns
    private static final List<Pattern> CRISIS_PATTERNS = Arrays.asList(
            Pattern.compile("(?i).*(suicid|kill myself|end my life|want to die|better off dead).*"),
            Pattern.compile("(?i).*(self.?harm|cut myself|hurt myself|harm myself).*"),
            Pattern.compile("(?i).*(no reason to live|nothing to live for|can't go on).*"),
            Pattern.compile("(?i).*(ending it all|final goodbye|last message).*"),
            Pattern.compile("(?i).*(overdose|take all the pills|jump off).*")
    );

    private static final List<Pattern> DISTRESS_PATTERNS = Arrays.asList(
            Pattern.compile("(?i).*(feeling hopeless|no hope|give up|can't cope).*"),
            Pattern.compile("(?i).*(extremely anxious|panic attack|can't breathe).*"),
            Pattern.compile("(?i).*(so depressed|deeply sad|unbearable pain).*"),
            Pattern.compile("(?i).*(all alone|nobody cares|no one understands).*")
    );

    // ==================== CONVERSATION MANAGEMENT ====================

    @Override
    public ChatConversationDTO startConversation(Long userId, String platform) {
        User user = findUserById(userId);

        // Check for existing active conversation
        Optional<ChatConversation> existing = conversationRepository.findActiveByUserId(userId);
        if (existing.isPresent()) {
            return ChatConversationDTO.fromEntity(existing.get());
        }

        ChatConversation conversation = new ChatConversation(user);
        conversation.setClientPlatform(platform);
        conversation.setModelVersion(mlServiceClient.getAvailableModelVersions().stream()
                .findFirst().orElse("default"));

        ChatConversation saved = conversationRepository.save(conversation);

        // Add welcome message
        String welcomeMessage = generateWelcomeMessage(user);
        addBotMessage(saved, welcomeMessage, "welcome", 0L);

        return ChatConversationDTO.fromEntity(saved);
    }

    @Override
    public ChatConversationDTO startConversationWithContext(Long userId, String platform,
                                                            Double stressLevel, Double anxietyLevel,
                                                            Double depressionLevel) {
        ChatConversationDTO dto = startConversation(userId, platform);

        ChatConversation conversation = conversationRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setPreConversationStress(stressLevel);
        conversation.setPreConversationAnxiety(anxietyLevel);
        conversation.setPreConversationDepression(depressionLevel);

        conversationRepository.save(conversation);

        return ChatConversationDTO.fromEntity(conversation);
    }

    @Override
    public Optional<ChatConversationDTO> getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .map(ChatConversationDTO::fromEntity);
    }

    @Override
    public Optional<ChatConversationDTO> getConversationWithMessages(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conv -> ChatConversationDTO.fromEntity(conv, true));
    }

    @Override
    public Optional<ChatConversationDTO> getActiveConversation(Long userId) {
        return conversationRepository.findActiveByUserId(userId)
                .map(ChatConversationDTO::fromEntity);
    }

    @Override
    public List<Object> getUserConversationHistory(Long userId) {
        User user = findUserById(userId);
        return conversationRepository.findByUserOrderByStartedAtDesc(user).stream()
                .map(ChatConversationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Object> getUserConversationHistory(Long userId, int page, int size) {
        User user = findUserById(userId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
        return conversationRepository.findByUserOrderByStartedAtDesc(user).stream()
                .skip((long) page * size)
                .limit(size)
                .map(ChatConversationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ChatConversationDTO endConversation(Long conversationId) {
        ChatConversation conversation = findConversationById(conversationId);

        // Add closing message
        String closingMessage = generateClosingMessage(conversation);
        addBotMessage(conversation, closingMessage, "closing", 0L);

        conversation.endConversation();
        conversationRepository.save(conversation);

        return ChatConversationDTO.fromEntity(conversation);
    }

    @Override
    public ChatConversationDTO endConversationWithAssessment(Long conversationId,
                                                             Double postStress, Double postAnxiety,
                                                             Double postDepression) {
        ChatConversation conversation = findConversationById(conversationId);

        conversation.setPostConversationStress(postStress);
        conversation.setPostConversationAnxiety(postAnxiety);
        conversation.setPostConversationDepression(postDepression);

        conversation.endConversation();
        conversationRepository.save(conversation);

        return ChatConversationDTO.fromEntity(conversation);
    }

    @Override
    public ChatConversationDTO pauseConversation(Long conversationId) {
        ChatConversation conversation = findConversationById(conversationId);
        conversation.setStatus(ConversationStatus.PAUSED);
        conversationRepository.save(conversation);
        return ChatConversationDTO.fromEntity(conversation);
    }

    @Override
    public ChatConversationDTO resumeConversation(Long conversationId) {
        ChatConversation conversation = findConversationById(conversationId);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return ChatConversationDTO.fromEntity(conversation);
    }

    // ==================== MESSAGE PROCESSING ====================

    @Override
    public ChatResponse processMessage(Long userId, ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // Get or create conversation
            ChatConversation conversation;
            boolean isNew = false;

            if (request.getConversationId() != null) {
                conversation = findConversationById(request.getConversationId());
            } else {
                Optional<ChatConversation> existing = conversationRepository.findActiveByUserId(userId);
                if (existing.isPresent()) {
                    conversation = existing.get();
                } else {
                    ChatConversationDTO newConv = startConversation(userId, request.getPlatform());
                    conversation = findConversationById(newConv.getId());
                    isNew = true;
                }
            }

            // Analyze user message
            CrisisAnalysisResult crisisResult = analyzeCrisisIndicators(request.getMessage());
            SentimentAnalysisResult sentimentResult = analyzeSentiment(request.getMessage());

            // Store user message
            ChatMessage userMessage = createUserMessage(conversation, request.getMessage(),
                    sentimentResult, crisisResult);
            messageRepository.save(userMessage);
            conversation.addMessage(userMessage);

            // Handle crisis if detected
            if (crisisResult.crisisDetected() &&
                    (crisisResult.level() == CrisisLevel.HIGH || crisisResult.level() == CrisisLevel.CRITICAL)) {
                return handleCrisis(conversation.getId(), userMessage.getId(),
                        crisisResult.level(), crisisResult.indicators());
            }

            // Generate bot response
            String botResponse = generateResponse(conversation, request.getMessage(),
                    sentimentResult, crisisResult, request.getContext());

            long responseTime = System.currentTimeMillis() - startTime;

            // Store bot response
            ChatMessage botMessage = addBotMessage(conversation, botResponse,
                    determineResponseStrategy(sentimentResult, crisisResult), responseTime);

            conversationRepository.save(conversation);

            // Build response
            return ChatResponse.builder()
                    .conversationId(conversation.getId())
                    .sessionId(conversation.getSessionId())
                    .isNewConversation(isNew)
                    .messageId(botMessage.getId())
                    .message(botResponse)
                    .timestamp(botMessage.getTimestamp())
                    .responseTimeMs(responseTime)
                    .modelVersion(conversation.getModelVersion())
                    .responseStrategy(botMessage.getResponseStrategy())
                    .userMessageSentiment(sentimentResult.score())
                    .userMessageEmotion(sentimentResult.dominantEmotion())
                    .userMessageIntent(userMessage.getDetectedIntent())
                    .crisisDetected(crisisResult.crisisDetected())
                    .crisisLevel(crisisResult.level())
                    .crisisIndicators(crisisResult.indicators())
                    .conversationStatus(conversation.getStatus().name())
                    .messageCount(conversation.getTotalMessages())
                    .hasError(false)
                    .build();

        } catch (Exception e) {
            logger.error("Error processing message for user {}: {}", userId, e.getMessage(), e);
            return ChatResponse.error("I'm having trouble responding right now. Please try again.");
        }
    }

    @Override
    public ChatResponse processMessageInConversation(Long conversationId, String message,
                                                     Map<String, Object> context) {
        ChatConversation conversation = findConversationById(conversationId);

        ChatRequest request = new ChatRequest();
        request.setConversationId(conversationId);
        request.setMessage(message);
        request.setContext(context);

        return processMessage(conversation.getUser().getId(), request);
    }

    @Override
    public List<ChatMessageDTO> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrdered(conversationId).stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDTO> getConversationMessages(Long conversationId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return messageRepository.findLatestNByConversation(conversationId, pageRequest).stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDTO> getLastMessages(Long conversationId, int count) {
        PageRequest pageRequest = PageRequest.of(0, count);
        return messageRepository.findLatestNByConversation(conversationId, pageRequest).stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== CRISIS DETECTION & HANDLING ====================

    @Override
    public CrisisAnalysisResult analyzeCrisisIndicators(String message) {
        List<String> indicators = new ArrayList<>();
        CrisisLevel level = CrisisLevel.NONE;
        double confidence = 0.0;

        // Check for critical crisis patterns
        for (Pattern pattern : CRISIS_PATTERNS) {
            if (pattern.matcher(message).matches()) {
                indicators.add("Critical crisis language detected");
                level = CrisisLevel.CRITICAL;
                confidence = 0.95;
                break;
            }
        }

        // Check for distress patterns
        if (level == CrisisLevel.NONE) {
            for (Pattern pattern : DISTRESS_PATTERNS) {
                if (pattern.matcher(message).matches()) {
                    indicators.add("Significant distress indicators");
                    level = CrisisLevel.MEDIUM;
                    confidence = 0.8;
                    break;
                }
            }
        }

        // Try ML service for additional analysis
        if (mlServiceClient.isChatbotServiceHealthy()) {
            try {
                Optional<Map<String, Object>> mlResult = mlServiceClient.detectCrisisIndicators(message);
                if (mlResult.isPresent()) {
                    Map<String, Object> result = mlResult.get();
                    if (result.containsKey("crisis_detected") && (Boolean) result.get("crisis_detected")) {
                        String mlLevel = (String) result.getOrDefault("level", "MEDIUM");
                        CrisisLevel detectedLevel = CrisisLevel.valueOf(mlLevel);
                        if (detectedLevel.ordinal() > level.ordinal()) {
                            level = detectedLevel;
                        }
                        if (result.containsKey("indicators")) {
                            indicators.addAll((List<String>) result.get("indicators"));
                        }
                        confidence = Math.max(confidence,
                                (Double) result.getOrDefault("confidence", 0.7));
                    }
                }
            } catch (Exception e) {
                logger.warn("ML crisis detection failed, using pattern-based: {}", e.getMessage());
            }
        }

        List<String> recommendedActions = getRecommendedCrisisActions(level);

        return new CrisisAnalysisResult(
                level != CrisisLevel.NONE,
                level,
                indicators,
                confidence,
                recommendedActions
        );
    }

    @Override
    public ChatResponse handleCrisis(Long conversationId, Long messageId, CrisisLevel level,
                                     List<String> indicators) {
        ChatConversation conversation = findConversationById(conversationId);

        // Update conversation status
        conversation.setStatus(ConversationStatus.CRISIS_ACTIVE);
        conversation.setHasCrisisEvent(true);
        if (conversation.getHighestCrisisLevel() == null ||
                level.ordinal() > conversation.getHighestCrisisLevel().ordinal()) {
            conversation.setHighestCrisisLevel(level);
        }

        // Generate crisis response
        String crisisMessage = generateCrisisResponse(level);

        ChatMessage botMessage = addBotMessage(conversation, crisisMessage, "crisis_intervention", 0L);
        botMessage.setCrisisDetected(true);
        botMessage.setCrisisLevel(level);
        messageRepository.save(botMessage);

        // Trigger escalation for high/critical levels
        if (level == CrisisLevel.HIGH || level == CrisisLevel.CRITICAL) {
            conversation.setCrisisEscalated(true);
            // In production, this would trigger alerts to healthcare professionals
            logger.warn("CRISIS ALERT: User {} - Level: {} - Conversation: {}",
                    conversation.getUser().getId(), level, conversationId);
        }

        conversationRepository.save(conversation);

        return ChatResponse.builder()
                .conversationId(conversationId)
                .messageId(botMessage.getId())
                .message(crisisMessage)
                .timestamp(LocalDateTime.now())
                .crisisDetected(true)
                .crisisLevel(level)
                .crisisIndicators(indicators)
                .escalationTriggered(level == CrisisLevel.HIGH || level == CrisisLevel.CRITICAL)
                .escalationMessage(level == CrisisLevel.CRITICAL
                        ? "A crisis counselor has been notified and will reach out shortly."
                        : null)
                .suggestedActions(getRecommendedCrisisActions(level))
                .resourceSuggestions(getCrisisResources(level))
                .conversationStatus(ConversationStatus.CRISIS_ACTIVE.name())
                .build();
    }

    @Override
    public ChatConversationDTO escalateToProfessional(Long conversationId, String reason) {
        ChatConversation conversation = findConversationById(conversationId);
        conversation.escalateToProfessional("system_escalation");

        // Add system message
        ChatMessage systemMessage = new ChatMessage(conversation,
                "This conversation has been escalated to a healthcare professional. " +
                        "Someone will be with you shortly.", SenderType.SYSTEM);
        messageRepository.save(systemMessage);
        conversation.addMessage(systemMessage);

        conversationRepository.save(conversation);

        return ChatConversationDTO.fromEntity(conversation);
    }

    @Override
    public List<ChatMessageDTO> getUnhandledCrisisMessages() {
        return messageRepository.findUnhandledCrisisMessages().stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatConversationDTO> getActiveCrisisConversations() {
        return conversationRepository.findByStatus(ConversationStatus.CRISIS_ACTIVE).stream()
                .map(ChatConversationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void markCrisisHandled(Long messageId, String handledBy) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));
        message.resolveCrisis(handledBy);
        messageRepository.save(message);
    }

    // ==================== SENTIMENT ANALYSIS ====================

    @Override
    public SentimentAnalysisResult analyzeSentiment(String message) {
        double score = 0.0;
        List<String> emotions = new ArrayList<>();
        double confidence = 0.7;

        // Try ML service first
        if (mlServiceClient.isChatbotServiceHealthy()) {
            try {
                Optional<Map<String, Object>> mlResult = mlServiceClient.analyzeSentiment(message);
                if (mlResult.isPresent()) {
                    Map<String, Object> result = mlResult.get();
                    score = (Double) result.getOrDefault("score", 0.0);
                    emotions = (List<String>) result.getOrDefault("emotions", List.of());
                    confidence = (Double) result.getOrDefault("confidence", 0.8);
                }
            } catch (Exception e) {
                logger.warn("ML sentiment analysis failed, using fallback: {}", e.getMessage());
                score = analyzeSentimentFallback(message);
            }
        } else {
            score = analyzeSentimentFallback(message);
        }

        SentimentLabel label = ChatMessage.getSentimentLabel(score);
        String dominantEmotion = emotions.isEmpty() ? inferEmotionFromScore(score) : emotions.get(0);

        return new SentimentAnalysisResult(score, label, emotions, confidence, dominantEmotion);
    }

    @Override
    public List<Map<String, Object>> getConversationSentimentProgression(Long conversationId) {
        List<ChatMessage> messages = messageRepository.findByConversationIdOrdered(conversationId);

        return messages.stream()
                .filter(m -> m.getSentimentScore() != null)
                .map(m -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("timestamp", m.getTimestamp());
                    point.put("sentimentScore", m.getSentimentScore());
                    point.put("sentimentLabel", m.getSentimentLabel());
                    point.put("senderType", m.getSenderType());
                    return point;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserSentimentTrends(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        User user = findUserById(userId);

        List<ChatConversation> conversations = conversationRepository
                .findByUserAndStartedAtAfter(user, since);

        Map<String, Object> trends = new HashMap<>();

        if (!conversations.isEmpty()) {
            DoubleSummaryStatistics stats = conversations.stream()
                    .filter(c -> c.getAverageSentimentScore() != null)
                    .mapToDouble(ChatConversation::getAverageSentimentScore)
                    .summaryStatistics();

            trends.put("averageSentiment", stats.getAverage());
            trends.put("minSentiment", stats.getMin());
            trends.put("maxSentiment", stats.getMax());
            trends.put("conversationCount", conversations.size());

            // Calculate trend
            List<Double> sentiments = conversations.stream()
                    .filter(c -> c.getAverageSentimentScore() != null)
                    .sorted(Comparator.comparing(ChatConversation::getStartedAt))
                    .map(ChatConversation::getAverageSentimentScore)
                    .toList();

            if (sentiments.size() >= 2) {
                double first = sentiments.get(0);
                double last = sentiments.get(sentiments.size() - 1);
                String trend = last > first + 0.1 ? "IMPROVING" :
                        last < first - 0.1 ? "DECLINING" : "STABLE";
                trends.put("trend", trend);
            }
        }

        return trends;
    }

    // ==================== FEEDBACK ====================

    @Override
    public ChatMessageDTO addMessageFeedback(Long messageId, Integer rating, Boolean wasHelpful,
                                             String feedbackText) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        message.setUserFeedbackRating(rating);
        message.setWasHelpful(wasHelpful);
        message.setUserFeedbackText(feedbackText);

        messageRepository.save(message);
        return ChatMessageDTO.fromEntity(message);
    }

    @Override
    public ChatConversationDTO addConversationFeedback(Long conversationId, Integer rating,
                                                       Boolean wasHelpful, String feedback,
                                                       Boolean wouldUseAgain) {
        ChatConversation conversation = findConversationById(conversationId);

        conversation.setUserRating(rating);
        conversation.setWasHelpful(wasHelpful);
        conversation.setUserFeedback(feedback);
        conversation.setWouldUseAgain(wouldUseAgain);

        conversationRepository.save(conversation);
        return ChatConversationDTO.fromEntity(conversation);
    }

    // ==================== ANALYTICS ====================

    @Override
    public Map<String, Object> getUserChatStatistics(Long userId) {
        User user = findUserById(userId);
        List<ChatConversation> conversations = conversationRepository.findByUserOrderByStartedAtDesc(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConversations", conversations.size());

        if (!conversations.isEmpty()) {
            stats.put("totalMessages", conversations.stream()
                    .mapToInt(ChatConversation::getTotalMessages).sum());
            stats.put("averageSessionDuration", conversations.stream()
                    .filter(c -> c.getSessionDurationMinutes() != null)
                    .mapToInt(ChatConversation::getSessionDurationMinutes)
                    .average().orElse(0));
            stats.put("crisisEvents", conversations.stream()
                    .mapToInt(c -> c.getCrisisEventsCount() != null ? c.getCrisisEventsCount() : 0).sum());
            stats.put("averageRating", conversations.stream()
                    .filter(c -> c.getUserRating() != null)
                    .mapToInt(ChatConversation::getUserRating)
                    .average().orElse(0));
        }

        return stats;
    }

    @Override
    public Map<String, Object> getSystemChatStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalConversations", conversationRepository.count());
        stats.put("activeConversations", conversationRepository.countByStatus(ConversationStatus.ACTIVE));
        stats.put("crisisConversations", conversationRepository.countByStatus(ConversationStatus.CRISIS_ACTIVE));
        stats.put("averageBotRating", messageRepository.getAverageBotRating());

        return stats;
    }

    @Override
    public Map<String, Object> getCrisisStatistics(LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();

        List<ChatMessage> crisisMessages = messageRepository.findRecentCrisisMessages(since);
        stats.put("totalCrisisMessages", crisisMessages.size());

        Map<CrisisLevel, Long> byLevel = crisisMessages.stream()
                .collect(Collectors.groupingBy(ChatMessage::getCrisisLevel, Collectors.counting()));
        stats.put("byLevel", byLevel);

        long handled = crisisMessages.stream()
                .filter(m -> m.getCrisisHandledBy() != null).count();
        stats.put("handled", handled);
        stats.put("unhandled", crisisMessages.size() - handled);

        return stats;
    }

    // ==================== HELPER METHODS ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private ChatConversation findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
    }

    private ChatMessage createUserMessage(ChatConversation conversation, String content,
                                          SentimentAnalysisResult sentiment, CrisisAnalysisResult crisis) {
        ChatMessage message = new ChatMessage(conversation, content, SenderType.USER);

        message.setSentimentScore(sentiment.score());
        message.setSentimentLabel(sentiment.label());
        message.setEmotionDetected(String.join(",", sentiment.emotions()));
        message.setEmotionConfidence(sentiment.confidence());

        if (crisis.crisisDetected()) {
            message.markAsCrisis(crisis.level(), String.join(",", crisis.indicators()));
        }

        message.setDetectedIntent(detectIntent(content));

        return message;
    }

    private ChatMessage addBotMessage(ChatConversation conversation, String content,
                                      String strategy, Long responseTime) {
        ChatMessage message = new ChatMessage(conversation, content, SenderType.BOT);
        message.setResponseStrategy(strategy);
        message.setResponseTimeMs(responseTime);
        message.setModelVersion(conversation.getModelVersion());
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);
        conversation.addMessage(message);

        return message;
    }

    private String generateWelcomeMessage(User user) {
        String name = user.getFirstName() != null ? user.getFirstName() : "there";
        return String.format(
                "Hi %s! I'm here to listen and support you. How are you feeling today? " +
                        "Feel free to share whatever's on your mind - this is a safe space.", name);
    }

    private String generateClosingMessage(ChatConversation conversation) {
        if (conversation.getHasCrisisEvent() != null && conversation.getHasCrisisEvent()) {
            return "Thank you for reaching out today. Remember, you're not alone, and help is always available. " +
                    "Please don't hesitate to reach out again or contact a crisis helpline if you need immediate support.";
        }
        return "Thank you for chatting with me today. Remember, it's okay to reach out whenever you need support. " +
                "Take care of yourself, and I'm here whenever you want to talk again.";
    }

    private String generateResponse(ChatConversation conversation, String userMessage,
                                    SentimentAnalysisResult sentiment, CrisisAnalysisResult crisis,
                                    Map<String, Object> context) {
        // Try ML service first
        if (mlServiceClient.isChatbotServiceHealthy()) {
            try {
                Map<String, Object> chatContext = new HashMap<>();
                chatContext.put("sentiment", sentiment);
                chatContext.put("conversationHistory", conversation.getLastNMessages(5).stream()
                        .map(ChatMessage::getContent).toList());
                if (context != null) {
                    chatContext.putAll(context);
                }

                Optional<String> mlResponse = mlServiceClient.getChatbotResponse(
                        conversation.getUser().getId().toString(), userMessage, chatContext);

                if (mlResponse.isPresent()) {
                    return mlResponse.get();
                }
            } catch (Exception e) {
                logger.warn("ML chatbot service failed, using fallback: {}", e.getMessage());
            }
        }

        // Fallback to rule-based responses
        return generateFallbackResponse(sentiment, crisis);
    }

    private String generateFallbackResponse(SentimentAnalysisResult sentiment, CrisisAnalysisResult crisis) {
        if (sentiment.score() < -0.5) {
            return "I hear that you're going through a really difficult time. " +
                    "It takes courage to share these feelings. Would you like to tell me more about what's been happening?";
        } else if (sentiment.score() < 0) {
            return "It sounds like things have been challenging. I'm here to listen. " +
                    "What would be most helpful for you right now?";
        } else if (sentiment.score() > 0.5) {
            return "I'm glad to hear things are going well! What's been contributing to these positive feelings?";
        } else {
            return "Thank you for sharing that with me. How are these experiences affecting you day to day?";
        }
    }

    private String generateCrisisResponse(CrisisLevel level) {
        if (level == CrisisLevel.CRITICAL) {
            return "I'm really concerned about what you've shared, and I want you to know that you matter. " +
                    "Please reach out to a crisis helpline right now:\n\n" +
                    "🆘 National Suicide Prevention Lifeline: 988\n" +
                    "🆘 Crisis Text Line: Text HOME to 741741\n\n" +
                    "A professional can provide immediate support. Would you like me to stay with you while you reach out?";
        } else if (level == CrisisLevel.HIGH) {
            return "I hear how much pain you're in right now, and I'm glad you're talking to me. " +
                    "What you're feeling is serious, and you deserve support. " +
                    "Have you considered talking to a mental health professional or calling a helpline? " +
                    "I'm here with you.";
        } else {
            return "It sounds like you're going through something really tough. " +
                    "I want you to know that these feelings are valid, and there are people who want to help. " +
                    "Would it help to talk about what's been weighing on you?";
        }
    }

    private String determineResponseStrategy(SentimentAnalysisResult sentiment, CrisisAnalysisResult crisis) {
        if (crisis.crisisDetected()) {
            return "crisis_intervention";
        } else if (sentiment.score() < -0.5) {
            return "empathetic_support";
        } else if (sentiment.score() < 0) {
            return "active_listening";
        } else {
            return "supportive_engagement";
        }
    }

    private String detectIntent(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("help") || lowerMessage.contains("what should i do")) {
            return "seeking_advice";
        } else if (lowerMessage.contains("feel") || lowerMessage.contains("emotion")) {
            return "expressing_emotion";
        } else if (lowerMessage.contains("?")) {
            return "asking_question";
        } else if (lowerMessage.length() > 200) {
            return "venting";
        } else {
            return "general_sharing";
        }
    }

    private double analyzeSentimentFallback(String message) {
        String lower = message.toLowerCase();

        // Simple keyword-based sentiment
        int positive = 0, negative = 0;

        String[] positiveWords = {"happy", "good", "great", "better", "hope", "grateful", "thank", "love", "excited"};
        String[] negativeWords = {"sad", "bad", "terrible", "worse", "hopeless", "hate", "angry", "scared", "anxious", "depressed"};

        for (String word : positiveWords) {
            if (lower.contains(word)) positive++;
        }
        for (String word : negativeWords) {
            if (lower.contains(word)) negative++;
        }

        if (positive + negative == 0) return 0.0;
        return (positive - negative) / (double)(positive + negative);
    }

    private String inferEmotionFromScore(double score) {
        if (score < -0.6) return "distressed";
        if (score < -0.2) return "sad";
        if (score < 0.2) return "neutral";
        if (score < 0.6) return "content";
        return "happy";
    }

    private List<String> getRecommendedCrisisActions(CrisisLevel level) {
        List<String> actions = new ArrayList<>();

        if (level == CrisisLevel.CRITICAL || level == CrisisLevel.HIGH) {
            actions.add("Contact emergency services if in immediate danger");
            actions.add("Call a crisis helpline (988 in US)");
            actions.add("Reach out to a trusted person");
            actions.add("Go to your nearest emergency room");
        } else if (level == CrisisLevel.MEDIUM) {
            actions.add("Talk to a mental health professional");
            actions.add("Reach out to a trusted friend or family member");
            actions.add("Practice grounding techniques");
        }

        return actions;
    }

    private List<ChatResponse.ResourceSuggestion> getCrisisResources(CrisisLevel level) {
        List<ChatResponse.ResourceSuggestion> resources = new ArrayList<>();

        if (level == CrisisLevel.CRITICAL || level == CrisisLevel.HIGH) {
            resources.add(ChatResponse.ResourceSuggestion.builder()
                    .title("National Suicide Prevention Lifeline")
                    .description("24/7 free support")
                    .url("tel:988")
                    .type("hotline")
                    .build());
            resources.add(ChatResponse.ResourceSuggestion.builder()
                    .title("Crisis Text Line")
                    .description("Text HOME to 741741")
                    .url("sms:741741")
                    .type("hotline")
                    .build());
        }

        resources.add(ChatResponse.ResourceSuggestion.builder()
                .title("Grounding Exercises")
                .description("5-4-3-2-1 technique for anxiety")
                .url("/resources/grounding")
                .type("exercise")
                .build());

        return resources;
    }
}