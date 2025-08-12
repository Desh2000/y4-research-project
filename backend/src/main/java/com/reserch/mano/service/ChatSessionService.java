package com.reserch.mano.service;

import com.reserch.mano.controller.dto.response.ChatSessionResponse;
import com.reserch.mano.model.ChatSession;
import com.reserch.mano.securuty.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatSessionService {

    ChatSessionResponse createChatSession(UserPrincipal userPrincipal, String sessionName,
                                          ChatSession.SessionType sessionType);

    ChatSessionResponse getChatSession(Long sessionId, UserPrincipal userPrincipal);

    List<ChatSessionResponse> getUserChatSessions(UserPrincipal userPrincipal);

    Page<ChatSessionResponse> getUserChatSessions(UserPrincipal userPrincipal, Pageable pageable);

    ChatSessionResponse getActiveSession(UserPrincipal userPrincipal);

    ChatSessionResponse activateSession(Long sessionId, UserPrincipal userPrincipal);

    void deactivateSession(Long sessionId, UserPrincipal userPrincipal);

    void deactivateAllUserSessions(UserPrincipal userPrincipal);

    ChatSessionResponse updateSessionSummary(Long sessionId, String summary, UserPrincipal userPrincipal);

    void deleteChatSession(Long sessionId, UserPrincipal userPrincipal);

    List<ChatSessionResponse> getSessionsByType(ChatSession.SessionType sessionType, UserPrincipal userPrincipal);

    List<ChatSessionResponse> getCrisisSessions();

    long getUserSessionCount(UserPrincipal userPrincipal);

    long getSessionCountByType(ChatSession.SessionType sessionType);
}
