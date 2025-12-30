package com.research.mano.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Provides comprehensive API documentation for the Mental Health Application
 */
@Configuration
public class OpenAPIConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080" + contextPath).description("Local Development Server"),
                        new Server().url("https://api.mano-health.com" + contextPath).description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication token")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("MANO - Mental Health Application API")
                .description("""
                    ## Mental Health Prediction and Clustering System
                    
                    MANO is a comprehensive mental health application that integrates four key ML components:
                    
                    ### 🤖 **Component 1: Privacy-Preserving Synthetic Data Generation**
                    - Generate synthetic mental health data using GAN, VAE, and Differential Privacy
                    - Protect user privacy while enabling research
                    - Balance privacy vs utility scores
                    
                    ### 🧠 **Component 2: LSTM Mental Health Prediction**
                    - Predict stress, depression, and anxiety scores (0.0-1.0 range)
                    - Calculate overall risk scores and track trends
                    - Auto-assign users to appropriate mental health clusters
                    
                    ### 💬 **Component 3: Empathetic Chatbot System**
                    - Crisis detection through keyword analysis
                    - Sentiment analysis (-1.0 to +1.0 range)
                    - Real-time intervention triggering
                    - Session management and conversation tracking
                    
                    ### 📊 **Component 4: GMM Clustering System**
                    - 9-cluster system (3×3 matrix: STRESS/DEPRESSION/ANXIETY × LOW/MEDIUM/HIGH)
                    - Dynamic cluster assignment and centroid updates
                    - Professional support level recommendations
                    
                    ### 🚨 **Crisis Management System**
                    - Real-time alert generation for high-risk situations
                    - Professional notification and assignment
                    - Emergency contact integration
                    - Intervention tracking and resolution
                    
                    ### 🔐 **Security Features**
                    - JWT-based authentication with refresh tokens
                    - Role-based access control (USER, HEALTHCARE_PROFESSIONAL, ADMIN, RESEARCHER)
                    - Email verification and password reset
                    - Comprehensive audit logging
                    
                    ---
                    
                    **Base URL:** `http://localhost:8080/api`
                    
                    **Authentication:** Bearer token required for most endpoints
                    
                    **Rate Limiting:** 100 requests per minute per user
                    
                    **API Version:** """ + appVersion + """
                    
                    ---
                    
                    ### 📚 **API Endpoint Categories:**
                    
                    - **Authentication** (`/auth/*`) - User registration, login, JWT management
                    - **Mental Health Predictions** (`/mental-health/predictions/*`) - LSTM predictions and risk analysis
                    - **User Profiles** (`/mental-health/profiles/*`) - Profile management and cluster assignment
                    - **Clustering** (`/mental-health/clusters/*`) - GMM clustering system
                    - **Chat System** (`/chat/*`) - Chatbot interactions and crisis detection
                    - **System Alerts** (`/alerts/*`) - Crisis management and notifications
                    - **Synthetic Data** (`/synthetic-data/*`) - Privacy-preserving data generation
                    
                    ### 🏥 **Healthcare Professional Features:**
                    
                    Healthcare professionals have additional access to:
                    - View all user mental health data
                    - Manage crisis alerts and interventions
                    - Access comprehensive analytics and reporting
                    - Assign users to different clusters
                    - Generate quality metrics reports
                    
                    ### 👨‍💼 **Admin Features:**
                    
                    Administrators can:
                    - Manage system-wide settings
                    - Initialize and maintain clustering system
                    - Access all user data and system metrics
                    - Manage user roles and permissions
                    - Perform system maintenance tasks
                    
                    ### 🔬 **Researcher Features:**
                    
                    Researchers can:
                    - Generate synthetic data for research
                    - Access anonymized datasets
                    - Export data for external analysis
                    - Validate synthetic data quality
                    
                    ---
                    
                    ### 📋 **Data Models:**
                    
                    **Mental Health Scores:** All scores are normalized to 0.0-1.0 range where:
                    - 0.0-0.3: Low risk
                    - 0.4-0.7: Medium risk  
                    - 0.8-1.0: High risk (triggers interventions)
                    
                    **Cluster System:** 9 clusters in 3×3 matrix:
                    - Categories: STRESS, DEPRESSION, ANXIETY
                    - Levels: LOW, MEDIUM, HIGH
                    - Example: STRESS_HIGH, DEPRESSION_MEDIUM
                    
                    **Alert Severity:** Four levels:
                    - LOW: Informational
                    - MEDIUM: Requires attention
                    - HIGH: Urgent response needed
                    - CRITICAL: Immediate intervention required
                    
                    ---
                    
                    For technical support, contact the development team.
                    """)
                .version(appVersion)
                .contact(new Contact()
                        .name("MANO Development Team")
                        .email("support@mano-health.com")
                        .url("https://mano-health.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
