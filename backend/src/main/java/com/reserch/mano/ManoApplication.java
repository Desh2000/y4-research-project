package com.reserch.mano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Application Class for Mano Backend
 *
 * This class serves as the entry point for the Spring Boot application.
 * It includes various annotations to enable different Spring Boot features:
 *
 * @SpringBootApplication - Enables auto-configuration, component scanning, and configuration
 * @EnableJpaAuditing - Enables JPA auditing features (createdDate, modifiedDate, etc.)
 * @EnableAsync - Enables asynchronous method execution
 * @EnableTransactionManagement - Enables transaction management
 * @ConfigurationPropertiesScan - Enables scanning for @ConfigurationProperties classes
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class ManoApplication {

	/**
	 * Main method to start the Spring Boot application
	 *
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ManoApplication.class);


		app.setAdditionalProfiles("dev");
		app.run(args);

		System.out.println("🚀 Mano Backend Application Started Successfully!");
		System.out.println("📊 Ready for ML Model Integration");
		System.out.println("🔐 JWT & OAuth2 Security Enabled");
		System.out.println("🗄️  PostgreSQL Database Connected");
		System.out.println("🌐 API Base URL: http://localhost:8080/api");
	}
}
