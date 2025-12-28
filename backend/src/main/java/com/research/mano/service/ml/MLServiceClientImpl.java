package com.research.mano.service.ml;

import com.research.mano.config.MLServiceProperties;
import com.research.mano.dto.ml.PredictionInput;
import com.research.mano.dto.ml.PredictionOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Implementation of ML Service Client
 * Handles communication with Python ML microservices
 */
@Service
public class MLServiceClientImpl implements MLServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(MLServiceClientImpl.class);

    @Autowired
    @Qualifier("mlRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private MLServiceProperties mlProperties;

    // ==================== HEALTH CHECKS ====================

    @Override
    public boolean isLSTMServiceHealthy() {
        return checkServiceHealth(mlProperties.getLstm());
    }

    @Override
    public boolean isGANServiceHealthy() {
        return checkServiceHealth(mlProperties.getGan());
    }

    @Override
    public boolean isChatbotServiceHealthy() {
        return checkServiceHealth(mlProperties.getChatbot());
    }

    @Override
    public boolean isClusteringServiceHealthy() {
        return checkServiceHealth(mlProperties.getClustering());
    }

    @Override
    public Map<String, Boolean> getAllServicesHealth() {
        Map<String, Boolean> health = new HashMap<>();
        health.put("lstm", isLSTMServiceHealthy());
        health.put("gan", isGANServiceHealthy());
        health.put("chatbot", isChatbotServiceHealthy());
        health.put("clustering", isClusteringServiceHealthy());
        return health;
    }

    private boolean checkServiceHealth(MLServiceProperties.ServiceEndpoint endpoint) {
        if (!endpoint.isEnabled()) {
            return false;
        }

        try {
            String url = endpoint.getBaseUrl() + endpoint.getHealthEndpoint();
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            logger.warn("Health check failed for {}: {}", endpoint.getBaseUrl(), e.getMessage());
            return false;
        }
    }

    // ==================== LSTM PREDICTION (Component 2) ====================

    @Override
    public Optional<PredictionOutput> getPrediction(PredictionInput input) {
        if (!mlProperties.getLstm().isEnabled()) {
            logger.warn("LSTM service is disabled");
            return Optional.empty();
        }

        try {
            String url = mlProperties.getLstm().getBaseUrl() + "/predict";
            HttpHeaders headers = createHeaders(mlProperties.getLstm());
            HttpEntity<PredictionInput> request = new HttpEntity<>(input, headers);

            ResponseEntity<PredictionOutput> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, PredictionOutput.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                PredictionOutput output = response.getBody();
                enrichPredictionOutput(output);
                return Optional.of(output);
            }
        } catch (RestClientException e) {
            logger.error("Error calling LSTM prediction service: {}", e.getMessage());
        }

        // Return fallback prediction if service unavailable
        return Optional.of(createFallbackPrediction(input));
    }

    @Override
    public List<PredictionOutput> getBatchPredictions(List<PredictionInput> inputs) {
        List<PredictionOutput> outputs = new ArrayList<>();

        if (!mlProperties.getLstm().isEnabled()) {
            logger.warn("LSTM service is disabled, returning fallback predictions");
            inputs.forEach(input -> outputs.add(createFallbackPrediction(input)));
            return outputs;
        }

        try {
            String url = mlProperties.getLstm().getBaseUrl() + "/predict/batch";
            HttpHeaders headers = createHeaders(mlProperties.getLstm());
            HttpEntity<List<PredictionInput>> request = new HttpEntity<>(inputs, headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Process response - in real implementation, map to PredictionOutput
                return outputs;
            }
        } catch (RestClientException e) {
            logger.error("Error calling batch prediction: {}", e.getMessage());
            inputs.forEach(input -> outputs.add(createFallbackPrediction(input)));
        }

        return outputs;
    }

    @Override
    public Optional<PredictionOutput> getPredictionWithModel(PredictionInput input, String modelVersion) {
        input.setModelVersion(modelVersion);
        return getPrediction(input);
    }

    @Override
    public List<String> getAvailableModelVersions() {
        try {
            String url = mlProperties.getLstm().getBaseUrl() + "/models";
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            logger.error("Error fetching model versions: {}", e.getMessage());
        }
        return List.of("default");
    }

    /**
     * Create a fallback prediction when ML service is unavailable
     * Uses questionnaire-based heuristics
     */
    private PredictionOutput createFallbackPrediction(PredictionInput input) {
        PredictionOutput output = new PredictionOutput();
        output.setRequestId(UUID.randomUUID().toString());
        output.setUserId(input.getUserId());
        output.setTimestamp(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        output.setModelVersion("fallback-heuristic");

        // Calculate scores from questionnaire data if available
        Double stressScore = calculateStressFromQuestionnaire(input);
        Double depressionScore = calculateDepressionFromQuestionnaire(input);
        Double anxietyScore = calculateAnxietyFromQuestionnaire(input);

        output.setStressScore(stressScore);
        output.setDepressionScore(depressionScore);
        output.setAnxietyScore(anxietyScore);
        output.setOverallRiskScore((stressScore + depressionScore + anxietyScore) / 3.0);

        // Set confidence (lower for fallback)
        output.setStressConfidence(0.6);
        output.setDepressionConfidence(0.6);
        output.setAnxietyConfidence(0.6);
        output.setOverallConfidence(0.6);

        // Set risk levels
        output.setStressRiskLevel(PredictionOutput.RiskLevel.fromScore(stressScore));
        output.setDepressionRiskLevel(PredictionOutput.RiskLevel.fromScore(depressionScore));
        output.setAnxietyRiskLevel(PredictionOutput.RiskLevel.fromScore(anxietyScore));
        output.setOverallRiskLevel(PredictionOutput.RiskLevel.fromScore(output.getOverallRiskScore()));

        // Add warning about fallback
        output.setWarnings(List.of("Prediction generated using fallback heuristics - ML service unavailable"));
        output.setIsSimulated(false);
        output.setDataQualityScore(0.7);

        enrichPredictionOutput(output);

        return output;
    }

    private Double calculateStressFromQuestionnaire(PredictionInput input) {
        // PSS score ranges from 0-40, normalize to 0-1
        Integer pssScore = input.calculatePSSScore();
        if (pssScore != null) {
            return Math.min(1.0, pssScore / 40.0);
        }
        return 0.5; // Default moderate
    }

    private Double calculateDepressionFromQuestionnaire(PredictionInput input) {
        // PHQ-9 score ranges from 0-27, normalize to 0-1
        Integer phq9Score = input.calculatePHQ9Score();
        if (phq9Score != null) {
            return Math.min(1.0, phq9Score / 27.0);
        }
        return 0.5;
    }

    private Double calculateAnxietyFromQuestionnaire(PredictionInput input) {
        // GAD-7 score ranges from 0-21, normalize to 0-1
        Integer gad7Score = input.calculateGAD7Score();
        if (gad7Score != null) {
            return Math.min(1.0, gad7Score / 21.0);
        }
        return 0.5;
    }

    private void enrichPredictionOutput(PredictionOutput output) {
        // Set cluster identifier
        if (output.getPrimaryClusterCategory() == null) {
            output.setPrimaryClusterCategory(output.getDominantCategory());
        }

        if (output.getPrimaryClusterLevel() == null && output.getOverallRiskScore() != null) {
            if (output.getOverallRiskScore() >= 0.7) {
                output.setPrimaryClusterLevel("HIGH");
            } else if (output.getOverallRiskScore() >= 0.4) {
                output.setPrimaryClusterLevel("MEDIUM");
            } else {
                output.setPrimaryClusterLevel("LOW");
            }
        }

        if (output.getClusterIdentifier() == null &&
                output.getPrimaryClusterCategory() != null &&
                output.getPrimaryClusterLevel() != null) {
            output.setClusterIdentifier(
                    output.getPrimaryClusterCategory() + "_" + output.getPrimaryClusterLevel());
        }

        // Check for crisis
        output.setRequiresImmediateAttention(output.hasHighRisk());
    }

    // ==================== GAN SERVICE (Component 1) ====================

    @Override
    public Optional<Map<String, Object>> generateSyntheticData(Map<String, Object> parameters) {
        if (!mlProperties.getGan().isEnabled()) {
            return Optional.empty();
        }

        try {
            String url = mlProperties.getGan().getBaseUrl() + "/generate";
            HttpHeaders headers = createHeaders(mlProperties.getGan());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(parameters, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error calling GAN service: {}", e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Object>> simulateInterventionOutcome(
            Long interventionId,
            Double preStress, Double preDepression, Double preAnxiety,
            Map<String, Object> additionalParams) {

        if (!mlProperties.getGan().isEnabled()) {
            return createSimulatedOutcomeFallback(interventionId, preStress, preDepression, preAnxiety);
        }

        try {
            String url = mlProperties.getGan().getBaseUrl() + "/simulate/intervention";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("interventionId", interventionId);
            requestBody.put("preStress", preStress);
            requestBody.put("preDepression", preDepression);
            requestBody.put("preAnxiety", preAnxiety);
            if (additionalParams != null) {
                requestBody.putAll(additionalParams);
            }

            HttpHeaders headers = createHeaders(mlProperties.getGan());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error simulating intervention: {}", e.getMessage());
        }

        return createSimulatedOutcomeFallback(interventionId, preStress, preDepression, preAnxiety);
    }

    private Optional<Map<String, Object>> createSimulatedOutcomeFallback(
            Long interventionId, Double preStress, Double preDepression, Double preAnxiety) {

        // Simple simulation: assume 10-20% improvement
        Random random = new Random();
        double improvementFactor = 0.1 + random.nextDouble() * 0.1;

        Map<String, Object> result = new HashMap<>();
        result.put("interventionId", interventionId);
        result.put("preStress", preStress);
        result.put("preDepression", preDepression);
        result.put("preAnxiety", preAnxiety);
        result.put("postStress", Math.max(0, preStress - improvementFactor));
        result.put("postDepression", Math.max(0, preDepression - improvementFactor));
        result.put("postAnxiety", Math.max(0, preAnxiety - improvementFactor));
        result.put("confidence", 0.6);
        result.put("isFallback", true);

        return Optional.of(result);
    }

    @Override
    public Optional<Map<String, Object>> getSyntheticDataQuality(String datasetId) {
        if (!mlProperties.getGan().isEnabled()) {
            return Optional.empty();
        }

        try {
            String url = mlProperties.getGan().getBaseUrl() + "/quality/" + datasetId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error getting data quality: {}", e.getMessage());
        }

        return Optional.empty();
    }

    // ==================== CHATBOT SERVICE (Component 3) ====================

    @Override
    public Optional<String> getChatbotResponse(String userId, String message, Map<String, Object> context) {
        if (!mlProperties.getChatbot().isEnabled()) {
            return Optional.of("I'm currently unavailable. Please try again later or contact support if you need immediate help.");
        }

        try {
            String url = mlProperties.getChatbot().getBaseUrl() + "/chat";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("message", message);
            requestBody.put("context", context);

            HttpHeaders headers = createHeaders(mlProperties.getChatbot());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Optional.ofNullable((String) response.getBody().get("response"));
            }
        } catch (RestClientException e) {
            logger.error("Error calling chatbot service: {}", e.getMessage());
        }

        return Optional.of("I'm having trouble responding right now. Please try again shortly.");
    }

    @Override
    public Optional<Map<String, Object>> analyzeSentiment(String text) {
        if (!mlProperties.getChatbot().isEnabled()) {
            return Optional.empty();
        }

        try {
            String url = mlProperties.getChatbot().getBaseUrl() + "/analyze/sentiment";

            Map<String, String> requestBody = Map.of("text", text);
            HttpHeaders headers = createHeaders(mlProperties.getChatbot());
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error analyzing sentiment: {}", e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<Map<String, Object>> detectCrisisIndicators(String text) {
        if (!mlProperties.getChatbot().isEnabled()) {
            return Optional.empty();
        }

        try {
            String url = mlProperties.getChatbot().getBaseUrl() + "/analyze/crisis";

            Map<String, String> requestBody = Map.of("text", text);
            HttpHeaders headers = createHeaders(mlProperties.getChatbot());
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error detecting crisis indicators: {}", e.getMessage());
        }

        return Optional.empty();
    }

    // ==================== CLUSTERING SERVICE (Component 4) ====================

    @Override
    public Optional<Map<String, Object>> getClusterAssignment(
            Double stressScore, Double depressionScore, Double anxietyScore) {

        if (!mlProperties.getClustering().isEnabled()) {
            return createFallbackClusterAssignment(stressScore, depressionScore, anxietyScore);
        }

        try {
            String url = mlProperties.getClustering().getBaseUrl() + "/assign";

            Map<String, Double> requestBody = Map.of(
                    "stressScore", stressScore,
                    "depressionScore", depressionScore,
                    "anxietyScore", anxietyScore
            );

            HttpHeaders headers = createHeaders(mlProperties.getClustering());
            HttpEntity<Map<String, Double>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error getting cluster assignment: {}", e.getMessage());
        }

        return createFallbackClusterAssignment(stressScore, depressionScore, anxietyScore);
    }

    private Optional<Map<String, Object>> createFallbackClusterAssignment(
            Double stress, Double depression, Double anxiety) {

        // Determine primary category
        String category;
        double maxScore = Math.max(stress, Math.max(depression, anxiety));

        if (maxScore == stress) category = "STRESS";
        else if (maxScore == depression) category = "DEPRESSION";
        else category = "ANXIETY";

        // Determine level
        String level;
        if (maxScore >= 0.7) level = "HIGH";
        else if (maxScore >= 0.4) level = "MEDIUM";
        else level = "LOW";

        Map<String, Object> result = new HashMap<>();
        result.put("clusterIdentifier", category + "_" + level);
        result.put("primaryCategory", category);
        result.put("primaryLevel", level);
        result.put("confidence", 0.7);
        result.put("isFallback", true);

        return Optional.of(result);
    }

    @Override
    public boolean updateClusterModel(List<Map<String, Object>> newData) {
        if (!mlProperties.getClustering().isEnabled()) {
            return false;
        }

        try {
            String url = mlProperties.getClustering().getBaseUrl() + "/model/update";
            HttpHeaders headers = createHeaders(mlProperties.getClustering());
            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(newData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (RestClientException e) {
            logger.error("Error updating cluster model: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public Optional<Map<String, Object>> getClusterStatistics() {
        if (!mlProperties.getClustering().isEnabled()) {
            return Optional.empty();
        }

        try {
            String url = mlProperties.getClustering().getBaseUrl() + "/statistics";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error getting cluster statistics: {}", e.getMessage());
        }

        return Optional.empty();
    }

    // ==================== MODEL MANAGEMENT ====================

    @Override
    public boolean triggerModelRetraining(String serviceType, Map<String, Object> parameters) {
        MLServiceProperties.ServiceEndpoint endpoint = getEndpoint(serviceType);
        if (endpoint == null || !endpoint.isEnabled()) {
            return false;
        }

        try {
            String url = endpoint.getBaseUrl() + "/model/retrain";
            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(parameters, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);

            return response.getStatusCode() == HttpStatus.OK ||
                    response.getStatusCode() == HttpStatus.ACCEPTED;
        } catch (RestClientException e) {
            logger.error("Error triggering model retraining: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public Optional<Map<String, Object>> getModelMetrics(String serviceType) {
        MLServiceProperties.ServiceEndpoint endpoint = getEndpoint(serviceType);
        if (endpoint == null || !endpoint.isEnabled()) {
            return Optional.empty();
        }

        try {
            String url = endpoint.getBaseUrl() + "/model/metrics";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (RestClientException e) {
            logger.error("Error getting model metrics: {}", e.getMessage());
        }

        return Optional.empty();
    }

    // ==================== HELPER METHODS ====================

    private HttpHeaders createHeaders(MLServiceProperties.ServiceEndpoint endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        if (endpoint.getApiKey() != null && !endpoint.getApiKey().isEmpty()) {
            headers.set("X-API-Key", endpoint.getApiKey());
        }

        return headers;
    }

    private MLServiceProperties.ServiceEndpoint getEndpoint(String serviceType) {
        return switch (serviceType.toLowerCase()) {
            case "lstm", "prediction" -> mlProperties.getLstm();
            case "gan", "synthetic" -> mlProperties.getGan();
            case "chatbot", "nlp" -> mlProperties.getChatbot();
            case "clustering", "gmm" -> mlProperties.getClustering();
            default -> null;
        };
    }
}