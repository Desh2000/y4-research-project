package com.research.mano.service.ml;

import com.research.mano.config.MLServiceProperties;
import com.research.mano.dto.ml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * ML Service Client Implementation
 * Handles HTTP communication with Python ML microservices
 * Supports both legacy and new API patterns
 */
@Service
public class MLServiceClientImpl implements MLServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(MLServiceClientImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MLServiceProperties mlServiceProperties;

    // Default model versions
    private static final List<String> DEFAULT_MODEL_VERSIONS = Arrays.asList(
            "lstm-v1.0", "lstm-v1.1", "lstm-v2.0"
    );

    // ==================== HEALTH CHECKS ====================

    @Override
    public Map<String, Boolean> checkAllServicesHealth() {
        Map<String, Boolean> healthStatus = new HashMap<>();
        healthStatus.put("lstm", isLSTMServiceHealthy());
        healthStatus.put("gan", isGANServiceHealthy());
        healthStatus.put("chatbot", isChatbotServiceHealthy());
        healthStatus.put("clustering", isClusteringServiceHealthy());
        return healthStatus;
    }

    @Override
    public boolean isLSTMServiceHealthy() {
        return checkServiceHealth(mlServiceProperties.getLstm());
    }

    @Override
    public boolean isGANServiceHealthy() {
        return checkServiceHealth(mlServiceProperties.getGan());
    }

    @Override
    public boolean isChatbotServiceHealthy() {
        return checkServiceHealth(mlServiceProperties.getChatbot());
    }

    @Override
    public boolean isClusteringServiceHealthy() {
        return checkServiceHealth(mlServiceProperties.getClustering());
    }

    /**
     * Check if a service endpoint is healthy
     */
    private boolean checkServiceHealth(MLServiceProperties.ServiceEndpoint endpoint) {
        if (endpoint == null || !endpoint.isEnabled()) {
            logger.warn("Service endpoint is null or disabled");
            return false;
        }

        try {
            String healthUrl = endpoint.getBaseUrl() + endpoint.getHealthEndpoint();
            logger.debug("Checking health at: {}", healthUrl);

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    healthUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            boolean isHealthy = response.getStatusCode() == HttpStatus.OK;
            logger.info("Health check for {} returned: {}", endpoint.getBaseUrl(), isHealthy);
            return isHealthy;

        } catch (RestClientException e) {
            logger.warn("Health check failed for {}: {}", endpoint.getBaseUrl(), e.getMessage());
            return false;
        }
    }

    // ==================== MODEL VERSIONS ====================

    @Override
    public List<String> getAvailableModelVersions() {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getLstm();

        if (!endpoint.isConfigured()) {
            return DEFAULT_MODEL_VERSIONS;
        }

        try {
            String url = endpoint.getBaseUrl() + "/api/v1/models";
            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<String>>() {}
            );

            return response.getBody() != null ? response.getBody() : DEFAULT_MODEL_VERSIONS;

        } catch (RestClientException e) {
            logger.warn("Failed to get model versions: {}", e.getMessage());
            return DEFAULT_MODEL_VERSIONS;
        }
    }
    /**
     * Get chatbot response (legacy API with String userId)
     */
    /**
     * Get chatbot response (legacy API with String userId)
     */
    public Optional<String> getChatbotResponse(String odataId, String message, Map<String, Object> context) {
        try {
            Long userId = odataId != null ? Long.parseLong(odataId) : null;
            return getChatbotResponse(userId, message, context);
        } catch (NumberFormatException e) {
            return getChatbotResponse((Long) null, message, context);
        }
    }

    // ==================== COMPONENT 2: LSTM PREDICTIONS ====================

    @Override
    public PredictionOutput predictRisk(PredictionInput input) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getLstm();

        if (!endpoint.isConfigured()) {
            logger.error("LSTM service is not configured");
            return createErrorPredictionOutput(input, "LSTM service not configured");
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getPredictEndpoint());
            logger.info("Sending prediction request to: {}", url);

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<PredictionInput> request = new HttpEntity<>(input, headers);

            ResponseEntity<PredictionOutput> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    PredictionOutput.class
            );

            logger.info("Prediction completed for user: {}", input.getUserId());
            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Prediction request failed: {}", e.getMessage());
            return createErrorPredictionOutput(input, e.getMessage());
        }
    }

    @Override
    public List<PredictionOutput> predictRiskBatch(List<PredictionInput> inputs) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getLstm();

        if (!endpoint.isConfigured()) {
            logger.error("LSTM service is not configured");
            return inputs.stream()
                    .map(input -> createErrorPredictionOutput(input, "LSTM service not configured"))
                    .toList();
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getBatchPredictEndpoint());
            logger.info("Sending batch prediction request for {} users", inputs.size());

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<List<PredictionInput>> request = new HttpEntity<>(inputs, headers);

            ResponseEntity<List<PredictionOutput>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<PredictionOutput>>() {}
            );

            logger.info("Batch prediction completed for {} users", inputs.size());
            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Batch prediction request failed: {}", e.getMessage());
            return inputs.stream()
                    .map(input -> createErrorPredictionOutput(input, e.getMessage()))
                    .toList();
        }
    }

    // ==================== COMPONENT 4: GMM CLUSTERING ====================

    @Override
    public ClusteringOutput assignCluster(ClusteringInput input) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getClustering();

        if (!endpoint.isConfigured()) {
            logger.error("Clustering service is not configured");
            return createErrorClusteringOutput(input, "Clustering service not configured");
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getClusterEndpoint());
            logger.info("Sending clustering request to: {}", url);

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<ClusteringInput> request = new HttpEntity<>(input, headers);

            ResponseEntity<ClusteringOutput> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ClusteringOutput.class
            );

            logger.info("Clustering completed for user: {}", input.getOdataId());
            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Clustering request failed: {}", e.getMessage());
            return createErrorClusteringOutput(input, e.getMessage());
        }
    }

    @Override
    public List<ClusteringOutput> assignClusterBatch(List<ClusteringInput> inputs) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getClustering();

        if (!endpoint.isConfigured()) {
            logger.error("Clustering service is not configured");
            return inputs.stream()
                    .map(input -> createErrorClusteringOutput(input, "Clustering service not configured"))
                    .toList();
        }

        try {
            String url = endpoint.getFullUrl("/api/v1/cluster/batch");
            logger.info("Sending batch clustering request for {} users", inputs.size());

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<List<ClusteringInput>> request = new HttpEntity<>(inputs, headers);

            ResponseEntity<List<ClusteringOutput>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<ClusteringOutput>>() {}
            );

            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Batch clustering request failed: {}", e.getMessage());
            return inputs.stream()
                    .map(input -> createErrorClusteringOutput(input, e.getMessage()))
                    .toList();
        }
    }

    @Override
    public Map<String, Object> getClusterStatistics() {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getClustering();

        if (!endpoint.isConfigured()) {
            logger.error("Clustering service is not configured");
            return Collections.singletonMap("error", "Clustering service not configured");
        }

        try {
            String url = endpoint.getFullUrl("/api/v1/clusters/statistics");

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Get cluster statistics failed: {}", e.getMessage());
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    // ==================== COMPONENT 3: CHATBOT ====================

    @Override
    public ChatbotOutput generateChatResponse(ChatbotInput input) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getChatbot();

        if (!endpoint.isConfigured()) {
            logger.error("Chatbot service is not configured");
            return createErrorChatbotOutput(input, "Chatbot service not configured");
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getChatEndpoint());
            logger.info("Sending chat request to: {}", url);

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<ChatbotInput> request = new HttpEntity<>(input, headers);

            ResponseEntity<ChatbotOutput> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ChatbotOutput.class
            );

            ChatbotOutput output = response.getBody();

            if (output != null && Boolean.TRUE.equals(output.getCrisisDetected())) {
                logger.warn("CRISIS DETECTED for user {} - Level: {}",
                        input.getUserId(), output.getCrisisLevel());
            }

            return output;

        } catch (RestClientException e) {
            logger.error("Chat request failed: {}", e.getMessage());
            return createErrorChatbotOutput(input, e.getMessage());
        }
    }

    @Override
    public ChatbotOutput.SentimentResult analyzeSentimentNew(String message) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getChatbot();

        if (!endpoint.isConfigured()) {
            logger.error("Chatbot service is not configured");
            return ChatbotOutput.SentimentResult.builder()
                    .sentiment("UNKNOWN")
                    .sentimentScore(0.0)
                    .confidence(0.0)
                    .build();
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getSentimentEndpoint());

            HttpHeaders headers = createHeaders(endpoint);
            Map<String, String> body = Collections.singletonMap("message", message);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<ChatbotOutput.SentimentResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ChatbotOutput.SentimentResult.class
            );

            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Sentiment analysis failed: {}", e.getMessage());
            return ChatbotOutput.SentimentResult.builder()
                    .sentiment("UNKNOWN")
                    .sentimentScore(0.0)
                    .confidence(0.0)
                    .build();
        }
    }

    @Override
    public CrisisDetectionResult detectCrisis(String message, Long userId) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getChatbot();

        if (!endpoint.isConfigured()) {
            logger.error("Chatbot service is not configured");
            return CrisisDetectionResult.builder()
                    .crisisDetected(false)
                    .crisisLevel("UNKNOWN")
                    .build();
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getCrisisEndpoint());

            HttpHeaders headers = createHeaders(endpoint);
            Map<String, Object> body = new HashMap<>();
            body.put("message", message);
            body.put("user_id", userId);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<CrisisDetectionResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    CrisisDetectionResult.class
            );

            CrisisDetectionResult result = response.getBody();

            if (result != null && Boolean.TRUE.equals(result.getCrisisDetected())) {
                logger.warn("CRISIS DETECTED via direct check for user {} - Level: {}",
                        userId, result.getCrisisLevel());
            }

            return result;

        } catch (RestClientException e) {
            logger.error("Crisis detection failed: {}", e.getMessage());
            return CrisisDetectionResult.builder()
                    .crisisDetected(false)
                    .crisisLevel("UNKNOWN")
                    .build();
        }
    }

    // ==================== COMPONENT 1: GAN SYNTHETIC DATA ====================

    @Override
    public SyntheticDataOutput generateSyntheticData(SyntheticDataRequest request) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getGan();

        if (!endpoint.isConfigured()) {
            logger.error("GAN service is not configured");
            return SyntheticDataOutput.builder()
                    .requestId(request.getRequestId())
                    .recordsGenerated(0)
                    .build();
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getGenerateEndpoint());
            logger.info("Sending synthetic data generation request to: {}", url);

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<SyntheticDataRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<SyntheticDataOutput> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpRequest,
                    SyntheticDataOutput.class
            );

            logger.info("Synthetic data generation completed: {} records",
                    response.getBody() != null ? response.getBody().getRecordsGenerated() : 0);
            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Synthetic data generation failed: {}", e.getMessage());
            return SyntheticDataOutput.builder()
                    .requestId(request.getRequestId())
                    .recordsGenerated(0)
                    .build();
        }
    }

    @Override
    public InterventionSimulationOutput simulateIntervention(InterventionSimulationRequest request) {
        MLServiceProperties.ServiceEndpoint endpoint = mlServiceProperties.getGan();

        if (!endpoint.isConfigured()) {
            logger.error("GAN service is not configured");
            return InterventionSimulationOutput.builder()
                    .requestId(request.getRequestId())
                    .confidenceScore(0.0)
                    .build();
        }

        try {
            String url = endpoint.getFullUrl(endpoint.getSimulateEndpoint());
            logger.info("Sending intervention simulation request to: {}", url);

            HttpHeaders headers = createHeaders(endpoint);
            HttpEntity<InterventionSimulationRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<InterventionSimulationOutput> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpRequest,
                    InterventionSimulationOutput.class
            );

            logger.info("Intervention simulation completed for user: {}", request.getUserId());
            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Intervention simulation failed: {}", e.getMessage());
            return InterventionSimulationOutput.builder()
                    .requestId(request.getRequestId())
                    .confidenceScore(0.0)
                    .build();
        }
    }

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

       try {String url = endpoint.getBaseUrl() + "/model/metrics";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
           if (response.getStatusCode() == HttpStatus.OK) {
               return Optional.ofNullable(response.getBody());
           }
        } catch (RestClientException e) {
            logger.error("Error getting model metrics: {}", e.getMessage());
       }
        return Optional.empty();
    }

    private MLServiceProperties.ServiceEndpoint getEndpoint(String serviceType) {
//        return switch (serviceType.toLowerCase()) {
//            Object mlProperties = null;
//            case "lstm", "prediction" -> mlProperties.getLstm();
//           case "gan", "synthetic" -> mlProperties.getGan();
//           case "chatbot", "nlp" -> mlProperties.getChatbot();
//            case "clustering", "gmm" -> mlProperties.getClustering();
//           default -> null;
//       };
//    }
        return null;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create HTTP headers with authentication if required
     */
    private HttpHeaders createHeaders(MLServiceProperties.ServiceEndpoint endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        if (endpoint.getApiKey() != null && !endpoint.getApiKey().isEmpty()) {
            headers.set("X-API-Key", endpoint.getApiKey());
        }

        return headers;
    }

    /**
     * Create error prediction output when service fails
     */
    private PredictionOutput createErrorPredictionOutput(PredictionInput input, String errorMessage) {
        return PredictionOutput.builder()
                .requestId(input != null ? input.getRequestId() : null)
                .userId(input != null ? input.getUserId() : null)
                .stressScore(0.0)
                .depressionScore(0.0)
                .anxietyScore(0.0)
                .overallRiskScore(0.0)
                .overallConfidence(0.0)
                .warnings(Collections.singletonList("Service error: " + errorMessage))
                .build();
    }

    /**
     * Create error clustering output when service fails
     */
    private ClusteringOutput createErrorClusteringOutput(ClusteringInput input, String errorMessage) {
        return ClusteringOutput.builder()
                .requestId(input != null ? input.getRequestId() : null)
                .userId(input != null ? input.getOdataId() : null)
                .clusterIdentifier("UNKNOWN")
                .membershipProbability(0.0)
                .build();
    }

    /**
     * Create error chatbot output when service fails
     */
    private ChatbotOutput createErrorChatbotOutput(ChatbotInput input, String errorMessage) {
        return ChatbotOutput.builder()
                .requestId(input != null ? input.getRequestId() : null)
                .conversationId(input != null ? input.getConversationId() : null)
                .response("I'm sorry, I'm having trouble responding right now. Please try again later or contact support if you need immediate help.")
                .responseType("ERROR")
                .crisisDetected(false)
                .build();
    }
}