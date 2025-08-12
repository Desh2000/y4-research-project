package com.reserch.mano.service;

import com.reserch.mano.controller.dto.request.ChatMessageRequest;
import com.reserch.mano.controller.dto.response.ChatMessageResponse;
import com.reserch.mano.securuty.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatMessageService {

    ChatMessageResponse sendMessage(ChatMessageRequest messageRequest, UserPrincipal userPrincipal);

    ChatMessageResponse processUserMessage(String content, Long sessionId, UserPrincipal userPrincipal);

    ChatMessageResponse generateBotResponse(String userMessage, Long sessionId, UserPrincipal userPrincipal);

    List<ChatMessageResponse> getSessionMessages(Long sessionId, UserPrincipal userPrincipal);

    Page<ChatMessageResponse> getSessionMessages(Long sessionId, UserPrincipal userPrincipal, Pageable pageable);

    List<ChatMessageResponse> getRecentMessages(Long sessionId, int count, UserPrincipal userPrincipal);

    Page<ChatMessageResponse> getUserMessages(UserPrincipal userPrincipal, Pageable pageable);

    void updateSentimentScore(Long messageId, Double sentimentScore);

    void updateRiskIndicators(Long messageId, String riskIndicators);

    void markInterventionTriggered(Long messageId);

    List<ChatMessageResponse> getHighRiskMessages(Double sentimentThreshold);

    List<ChatMessageResponse> getTriggeredInterventions();

    Double getUserAverageSentiment(UserPrincipal userPrincipal);

    long getUserMessageCount(UserPrincipal userPrincipal);

    long getSessionMessageCount(Long sessionId);

    void deleteMessage(Long messageId, UserPrincipal userPrincipal);
}
