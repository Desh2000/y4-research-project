package com.research.mano.exception;

public class ConversationNotFoundException extends ManoException {
    public ConversationNotFoundException(Long conversationId) {
        super("CONVERSATION_NOT_FOUND", "Conversation not found with ID: " + conversationId);
    }
}
