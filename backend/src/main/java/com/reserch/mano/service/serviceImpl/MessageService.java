package com.reserch.mano.service.serviceImpl;

import com.reserch.mano.controller.dto.request.MessageDto;
import com.reserch.mano.controller.dto.request.SendMessageRequest;
import com.reserch.mano.model.User;

import java.util.List;

/**
 * Service interface for chat message-related operations.
 */
public interface MessageService {
    MessageDto sendMessage(SendMessageRequest request, User user);
    List<MessageDto> getConversationHistory(User user);
}
