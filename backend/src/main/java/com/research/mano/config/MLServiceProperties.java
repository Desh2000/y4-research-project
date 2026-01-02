package com.research.mano.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Configuration properties for ML microservices
 * Maps to application.properties ml.service.* prefix
 */
@Data
@Component
@ConfigurationProperties(prefix = "ml.service")
public class MLServiceProperties {

    /**
     * Component 1: Privacy-Preserving GAN Service
     */
    private ServiceEndpoint gan = new ServiceEndpoint();

    /**
     * Component 2: LSTM Risk Prediction Service
     */
    private ServiceEndpoint lstm = new ServiceEndpoint();

    /**
     * Component 3: Chatbot/NLP Service
     */
    private ServiceEndpoint chatbot = new ServiceEndpoint();

    /**
     * Component 4: GMM Clustering Service
     */
    private ServiceEndpoint clustering = new ServiceEndpoint();

    /**
     * Connection settings
     */
    private int connectionTimeout = 5000;
    private int readTimeout = 30000;
    private int maxRetries = 3;
    private int retryDelayMs = 1000;

    @Data
    public static class ServiceEndpoint {
        private String baseUrl = "http://localhost:5000";
        private String healthEndpoint = "/health";
        private boolean enabled = true;
        private String apiKey;
    }
}