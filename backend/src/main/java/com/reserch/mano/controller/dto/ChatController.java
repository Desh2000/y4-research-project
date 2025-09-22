package com.reserch.mano.controller.dto;

import com.reserch.mano.controller.dto.request.MessageDto;
import com.reserch.mano.controller.dto.request.SendMessageRequest;
import com.reserch.mano.model.User;
import com.reserch.mano.service.serviceImpl.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling chat-related API requests.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    /**
     * Endpoint for a user to send a message and get a bot response.
     * @param request The request body containing the message content.
     * @param user The currently authenticated user.
     * @return The bot's response message DTO.
     */
    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(@RequestBody SendMessageRequest request, @AuthenticationPrincipal User user) {
        MessageDto botResponse = messageService.sendMessage(request, user);
        return ResponseEntity.ok(botResponse);
    }

    /**
     * Endpoint to retrieve the conversation history for the authenticated user.
     * @param user The currently authenticated user.
     * @return A list of message DTOs representing the conversation history.
     */
    @GetMapping("/history")
    public ResponseEntity<List<MessageDto>> getConversationHistory(@AuthenticationPrincipal User user) {
        List<MessageDto> history = messageService.getConversationHistory(user);
        return ResponseEntity.ok(history);
    }
}

