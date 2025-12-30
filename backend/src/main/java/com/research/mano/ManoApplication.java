package com.research.mano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Application Class for Mano Backend
 * Man Holistic Framework for Personalized and Community-Driven
 * Cognitive, Emotional, and Resilient Vitality
 *
 * Features:
 * - Component 1: Privacy-Preserving Synthetic Mental Health Data Generation
 * - Component 2: Stress, Depression, Anxiety and Cognitive Risk Prediction (LSTM)
 * - Component 3: Empathetic Conversational Support System (Chatbot)
 * - Component 4: Community-Driven Resilience Clustering System (GMM)
 *
 * @author Mano Team
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
public class ManoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManoApplication.class, args);

        System.out.println("""
        
        ╔══════════════════════════════════════════════════════════════╗
        ║                    MANO BACKEND STARTED                      ║
        ║            Man Holistic Mental Health Framework              ║
        ║══════════════════════════════════════════════════════════════║
        ║  🔐 Security: JWT + OAuth2                                   ║
        ║  🧠 ML Components: 4 Models Ready                          ║
        ║  🌐 API Base URL: http://localhost:8080/api/v1             ║
        ║  📊 Health Check: http://localhost:8080/api/v1/actuator    ║
        ╚══════════════════════════════════════════════════════════════╝
        
        """);
    }
}
