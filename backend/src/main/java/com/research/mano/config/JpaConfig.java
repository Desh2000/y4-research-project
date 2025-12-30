package com.research.mano.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Configuration for enabling auditing
 * Enables automatic creation and update timestamp management
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // This class enables JPA auditing features
    // Automatically populates @CreatedDate and @LastModifiedDate fields
}