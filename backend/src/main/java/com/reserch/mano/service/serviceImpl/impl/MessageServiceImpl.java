package com.reserch.mano.service.serviceImpl.impl;

import com.reserch.mano.controller.dto.request.MessageDto;
import com.reserch.mano.controller.dto.request.SendMessageRequest;
import com.reserch.mano.model.Message;
import com.reserch.mano.model.User;
import com.reserch.mano.repository.MessageRepository;
import com.reserch.mano.service.serviceImpl.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for chat message-related operations.
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private MessageRepository messageRepository;

    @Override
    @Transactional
    public MessageDto sendMessage(SendMessageRequest request, User user) {
        // 1. Save the user's message
        Message userMessage = Message.builder()
                .user(user)
                .content(request.getContent())
                .sender("USER")
                .build();
        messageRepository.save(userMessage);

        // 2. Simulate calling the Python NLP model and getting a response
        // TODO: Replace this with an actual HTTP call to your Python service
        String botResponseContent = "Thank you for sharing. How did that make you feel?";

        // 3. Save the bot's response
        Message botMessage = Message.builder()
                .user(user)
                .content(botResponseContent)
                .sender("BOT")
                .build();
        Message savedBotMessage = messageRepository.save(botMessage);

        // 4. Return the bot's response
        return mapToDto(savedBotMessage);
    }

    @Override
    public List<MessageDto> getConversationHistory(User user) {
        return messageRepository.findAll().stream()
                .filter(message -> message.getUser().getId().equals(user.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private MessageDto mapToDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .sender(message.getSender())
                .sentAt(message.getSentAt())
                .userId(message.getUser().getId())
                .build();
    }
}