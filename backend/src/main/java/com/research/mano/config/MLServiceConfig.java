package com.research.mano.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for ML Service communication
 * Handles connection settings for Python ML services (Component 1, 2, 3, 4)
 */
@Configuration
public class MLServiceConfig {

    @Value("${ml.service.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${ml.service.read-timeout:30000}")
    private int readTimeout;

    /**
     * RestTemplate for ML service communication
     */
    @Bean(name = "mlRestTemplate")
    public RestTemplate mlRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }
}