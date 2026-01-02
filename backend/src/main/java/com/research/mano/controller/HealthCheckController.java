package com.research.mano.controller;

import com.research.mano.service.ml.MLServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides endpoints for monitoring system health
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Autowired
    private MLServiceClient mlServiceClient;

    @Autowired
    private DataSource dataSource;

    @Value("${spring.application.name:mano}")
    private String applicationName;

    /**
     * GET /api/health
     * Basic health check
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(health);
    }

    /**
     * GET /api/health/detailed
     * Detailed health check including all services
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("application", applicationName);
        health.put("timestamp", LocalDateTime.now());

        // Database health
        Map<String, Object> dbHealth = checkDatabaseHealth();
        health.put("database", dbHealth);

        // ML Services health
        Map<String, Object> mlHealth = checkMLServicesHealth();
        health.put("mlServices", mlHealth);

        // Overall status
        boolean allHealthy = "UP".equals(dbHealth.get("status")) &&
                (boolean) mlHealth.getOrDefault("anyServiceHealthy", false);
        health.put("status", allHealthy ? "UP" : "DEGRADED");

        return ResponseEntity.ok(health);
    }

    /**
     * GET /api/health/database
     * Database connectivity check
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        return ResponseEntity.ok(checkDatabaseHealth());
    }

    /**
     * GET /api/health/ml-services
     * ML services health check
     */
    @GetMapping("/ml-services")
    public ResponseEntity<Map<String, Object>> mlServicesHealth() {
        return ResponseEntity.ok(checkMLServicesHealth());
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);
            dbHealth.put("status", valid ? "UP" : "DOWN");
            dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
            dbHealth.put("url", connection.getMetaData().getURL());
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }

        return dbHealth;
    }

    private Map<String, Object> checkMLServicesHealth() {
        Map<String, Object> mlHealth = new HashMap<>();

        Map<String, Boolean> servicesStatus = mlServiceClient.checkAllServicesHealth();
        mlHealth.put("services", servicesStatus);

        // Check if any service is healthy
        boolean anyHealthy = servicesStatus.values().stream().anyMatch(Boolean::booleanValue);
        mlHealth.put("anyServiceHealthy", anyHealthy);

        // Count healthy services
        long healthyCount = servicesStatus.values().stream().filter(Boolean::booleanValue).count();
        mlHealth.put("healthyCount", healthyCount);
        mlHealth.put("totalCount", servicesStatus.size());

        // Overall ML services status
        if (healthyCount == servicesStatus.size()) {
            mlHealth.put("status", "UP");
        } else if (healthyCount > 0) {
            mlHealth.put("status", "PARTIAL");
        } else {
            mlHealth.put("status", "DOWN");
        }

        return mlHealth;
    }
}