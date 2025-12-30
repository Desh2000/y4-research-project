package com.research.mano.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Component 3 (Chatbot)
 * Enables real-time bidirectional communication for chat functionality
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker
        // /topic - for broadcasting to multiple subscribers
        // /queue - for point-to-point messaging (user-specific)
        config.enableSimpleBroker("/topic", "/queue");

        // Application destination prefix - messages from client to server
        config.setApplicationDestinationPrefixes("/app");

        // User destination prefix - for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that clients connect to
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback for browsers that don't support WebSocket

        // Alternative endpoint without SockJS for native WebSocket clients (mobile)
        registry.addEndpoint("/ws/chat-native")
                .setAllowedOriginPatterns("*");
    }
}