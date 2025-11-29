package com.scu.uob.dsa.cardiac_trader_backend.service.impl;

import com.scu.uob.dsa.cardiac_trader_backend.exception.ExternalAPIException;
import com.scu.uob.dsa.cardiac_trader_backend.service.HeartAPIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

    @Service
public class HeartAPIServiceImpl implements HeartAPIService {

    private static final Logger logger = LoggerFactory.getLogger(HeartAPIServiceImpl.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${heart.api.url:https://marcconrad.com/uob/heart}")
    private String heartApiUrl;
    
    @Value("${heart.api.timeout:5000}")
    private int timeout;

    public HeartAPIServiceImpl() {
        // Configure RestTemplate with request factory that follows redirects
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // Default timeout, will be updated by @Value
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
        // RestTemplate follows redirects (3xx) automatically by default
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches a heart puzzle from the Heart Game API with base64 encoded image
     * API Documentation: https://marcconrad.com/uob/heart/doc.php
     * Base URL: http://marcconrad.com/uob/heart/api.php
     * Uses out=json to get JSON response
     * 
     * @return HeartPuzzle containing base64 image data URI and correct answer
     * @throws ExternalAPIException if API call fails
     */
    @Override
    @SuppressWarnings("unchecked")
    public HeartPuzzle fetchPuzzle() throws ExternalAPIException {
        logger.info("=== Heart API fetchPuzzle START ===");
        logger.info("Heart API URL configured as: {}", heartApiUrl);
        
        try {
            // API endpoint: https://marcconrad.com/uob/heart/api.php
            // Parameters: out=json&base64=yes (returns JSON with base64 encoded image)
            // According to documentation: https://marcconrad.com/uob/heart/doc.php
            // - out=json is default, but we specify it explicitly
            // - base64=yes is needed to get base64 encoded image instead of URL
            String url = heartApiUrl + "/api.php?out=json&base64=yes";
            
            // Ensure we use HTTPS if URL doesn't already specify it
            if (url.startsWith("http://")) {
                url = url.replace("http://", "https://");
                logger.info("Converted HTTP to HTTPS: {}", url);
            }
            
            logger.info("Calling Heart API URL: {}", url);
            
            // First, try to get response as String to handle any content type
            logger.debug("Sending GET request to Heart API...");
            ResponseEntity<String> stringResponse = restTemplate.getForEntity(url, String.class);
            
            logger.info("Heart API Response Status: {}", stringResponse.getStatusCode());
            logger.info("Heart API Response Headers: {}", stringResponse.getHeaders());
            
            if (!stringResponse.getStatusCode().is2xxSuccessful()) {
                logger.error("Heart API returned non-2xx status: {}", stringResponse.getStatusCode());
                throw new ExternalAPIException("Heart API", 
                    "Failed to fetch puzzle - HTTP " + stringResponse.getStatusCode().value(), 
                    stringResponse.getStatusCode().value());
            }
            
            String responseBody = stringResponse.getBody();
            logger.info("Heart API Response Body Length: {}", responseBody != null ? responseBody.length() : 0);
            
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.error("Heart API returned empty response body");
                throw new ExternalAPIException("Heart API", 
                    "Empty response from Heart API", 
                    null);
            }
            
            // Log first 500 characters of response for debugging
            logger.info("Heart API Response Body (first 500 chars): {}", 
                responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
            
            // Check if response is JSON (starts with { or [)
            if (!responseBody.trim().startsWith("{") && !responseBody.trim().startsWith("[")) {
                logger.error("Heart API response is not JSON. Starts with: {}", 
                    responseBody.trim().substring(0, Math.min(50, responseBody.trim().length())));
                // If not JSON, it might be HTML or plain text
                throw new ExternalAPIException("Heart API", 
                    "Expected JSON but received: " + responseBody.substring(0, Math.min(100, responseBody.length())) + "...", 
                    null);
            }
            
            logger.debug("Parsing JSON response...");
            // Parse JSON string to Map using Jackson ObjectMapper
            Map<String, Object> body = objectMapper.readValue(responseBody, Map.class);
            logger.info("Successfully parsed JSON. Keys in response: {}", body.keySet());
            
            // Extract data from JSON response
            // API response typically contains:
            // - Image (as base64 string if base64=yes, or URL otherwise)
            // - Solution (correct answer number)
            logger.debug("Extracting base64 image from response...");
            String base64Image = extractBase64Image(body);
            logger.debug("Base64 image extracted. Length: {}", base64Image != null ? base64Image.length() : 0);
            
            logger.debug("Extracting solution from response...");
            Integer correctAnswer = extractSolution(body);
            logger.info("Extracted solution (correct answer): {}", correctAnswer);
            
            // Convert base64 string to data URI format for frontend display
            String imageDataUri = convertToDataUri(base64Image);
            logger.debug("Converted to data URI. Data URI length: {}", imageDataUri.length());
            
            // Generate a unique puzzle ID from timestamp
            String puzzleId = String.valueOf(System.currentTimeMillis());
            logger.info("Generated puzzle ID: {}", puzzleId);
            
            logger.info("=== Heart API fetchPuzzle SUCCESS ===");
            return new HeartPuzzle(imageDataUri, correctAnswer, puzzleId);
            
        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors (403, 404, etc.)
            HttpStatusCode status = e.getStatusCode();
            int statusCode = status.value();
            String errorBody = e.getResponseBodyAsString();
            logger.error("Heart API HttpClientErrorException - Status: {}, Message: {}", statusCode, e.getMessage());
            logger.error("Heart API Error Response Body: {}", errorBody);
            throw new ExternalAPIException("Heart API", 
                "HTTP " + statusCode + " Error: " + e.getMessage() + 
                (errorBody != null ? " - Response: " + errorBody.substring(0, Math.min(200, errorBody.length())) : ""), 
                statusCode);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.error("JSON parsing error: {}", e.getMessage(), e);
            throw new ExternalAPIException("Heart API", 
                "Failed to parse JSON response: " + e.getMessage(), 
                null);
        } catch (Exception e) {
            logger.error("Unexpected error calling Heart API: {}", e.getMessage(), e);
            throw new ExternalAPIException("Heart API", 
                "Error calling Heart API: " + e.getMessage(), 
                null);
        }
    }
    
    /**
     * Extracts base64 image string from API response
     * Tries multiple possible field names for base64 image data
     */
    private String extractBase64Image(Map<String, Object> data) {
        logger.debug("Extracting base64 image. Available keys in data: {}", data.keySet());
        // Try various possible field names for the base64 image
        // According to Heart API docs, when base64=yes, the image is in "question" key
        String[] possibleKeys = {"question", "image", "imageBase64", "image_base64", "base64", "data", "imageData", "heart_image", "heartImage"};
        for (String key : possibleKeys) {
            Object value = data.get(key);
            if (value != null) {
                logger.debug("Found image data in key: {}", key);
                String base64String = value.toString();
                // Remove data URI prefix if present
                if (base64String.startsWith("data:image")) {
                    int commaIndex = base64String.indexOf(',');
                    if (commaIndex > 0) {
                        base64String = base64String.substring(commaIndex + 1);
                    }
                }
                // Remove any whitespace/newlines that might be in the base64 string
                base64String = base64String.trim().replaceAll("\\s+", "");
                logger.info("Extracted base64 image from key '{}'. Length: {}", key, base64String.length());
                return base64String;
            }
        }
        logger.warn("Could not find image data in any of the expected keys: {}", String.join(", ", possibleKeys));
        return "";
    }
    
    /**
     * Converts base64 string to data URI format for frontend display
     * Format: data:image/png;base64,{base64String}
     */
    private String convertToDataUri(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return "";
        }
        
        // If already a data URI, return as-is
        if (base64String.startsWith("data:image")) {
            return base64String;
        }
        
        // Assume PNG format (most common for the Heart API)
        // Can be enhanced to detect image format from base64 header if needed
        return "data:image/png;base64," + base64String;
    }
    
    /**
     * Extracts solution (correct answer) from API response
     * Tries multiple possible field names
     */
    private Integer extractSolution(Map<String, Object> data) {
        logger.debug("Extracting solution. Available keys in data: {}", data.keySet());
        // Try various possible field names for the solution
        String[] possibleKeys = {"solution", "answer", "correctAnswer", "correct_answer", "result", "heart_count", "heartCount", "count"};
        for (String key : possibleKeys) {
            Object value = data.get(key);
            if (value != null) {
                logger.debug("Found solution in key: {} with value: {}", key, value);
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                try {
                    return Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    logger.debug("Could not parse solution value '{}' as integer for key {}", value, key);
                    // Continue to next key
                }
            }
        }
        logger.warn("Could not find solution in any of the expected keys: {}. Using default value 0", String.join(", ", possibleKeys));
        return 0; // Default fallback
    }
}

