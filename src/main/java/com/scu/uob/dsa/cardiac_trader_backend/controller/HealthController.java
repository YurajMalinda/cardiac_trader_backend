package com.scu.uob.dsa.cardiac_trader_backend.controller;

import com.scu.uob.dsa.cardiac_trader_backend.exception.ExternalAPIException;
import com.scu.uob.dsa.cardiac_trader_backend.service.HeartAPIService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HeartAPIService heartAPIService;

    public HealthController(HeartAPIService heartAPIService) {
        this.heartAPIService = heartAPIService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "cardiac-trader-backend");
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to check Heart API integration
     * GET /api/health/heart-test
     */
    @GetMapping("/heart-test")
    public ResponseEntity<Map<String, Object>> testHeartAPI() {
        Map<String, Object> response = new HashMap<>();
        try {
            HeartAPIService.HeartPuzzle puzzle = heartAPIService.fetchPuzzle();

            response.put("status", "SUCCESS");
            response.put("message", "Heart API is working!");
            response.put("puzzleId", puzzle.getPuzzleId());
            response.put("correctAnswer", puzzle.getCorrectAnswer());
            response.put("imageUrl", puzzle.getImageUrl());
            response.put("imagePreview",
                    puzzle.getImageUrl().substring(0, Math.min(100, puzzle.getImageUrl().length())) + "...");

            // Set Content-Type header for frontend compatibility
            // CORS headers are handled by CorsConfig globally
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity.ok().headers(headers).body(response);
        } catch (ExternalAPIException e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to fetch from Heart API");
            response.put("error", e.getMessage());
            response.put("statusCode", e.getStatusCode());
            // Return the status code from the exception if available
            int statusCode = e.getStatusCode() != null ? e.getStatusCode() : 500;
            return ResponseEntity.status(statusCode).body(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Unexpected error");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}
