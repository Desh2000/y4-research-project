package com.reserch.mano.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Main security configuration class for the application.
 * Enables web security and configures security filters, password encoding, and authorization rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Creates a PasswordEncoder bean to be used for hashing and verifying passwords.
     * We are using BCrypt, which is a strong, widely-used hashing algorithm.
     *
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain that applies to all HTTP requests.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery) protection.
                // This is common for stateless REST APIs that use tokens for authentication.
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization rules for different endpoints.
                .authorizeHttpRequests(authorize -> authorize
                        // Allow all requests to endpoints under /api/auth/ (e.g., signup, login).
                        .requestMatchers("/api/auth/**").permitAll()
                        // All other requests must be authenticated.
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}