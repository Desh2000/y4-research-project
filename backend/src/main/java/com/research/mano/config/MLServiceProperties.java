package com.research.mano.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for ML Service endpoints
 * Maps to ml.service.* properties in application.properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ml.service")
public class MLServiceProperties {

    private ServiceEndpoint lstm = new ServiceEndpoint();
    private ServiceEndpoint gan = new ServiceEndpoint();
    private ServiceEndpoint chatbot = new ServiceEndpoint();
    private ServiceEndpoint clustering = new ServiceEndpoint();

    @Data
    public static class ServiceEndpoint {
        private boolean enabled = false;
        private String baseUrl;
        private String healthEndpoint = "/health";
        private String apiKey;
        private int timeout = 30000;

        // Component-specific endpoints
        private String predictEndpoint;
        private String batchPredictEndpoint;
        private String generateEndpoint;
        private String simulateEndpoint;
        private String chatEndpoint;
        private String sentimentEndpoint;
        private String crisisEndpoint;
        private String clusterEndpoint;
        private String analyzeEndpoint;
        private String recommendEndpoint;

        /**
         * Get full URL for a specific endpoint
         */
        public String getFullUrl(String endpoint) {
            if (baseUrl == null || endpoint == null) {
                return null;
            }
            String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
            return base + path;
        }

        /**
         * Check if this service is configured and enabled
         */
        public boolean isConfigured() {
            return enabled && baseUrl != null && !baseUrl.isEmpty();
        }
    }
}