package com.scu.uob.dsa.cardiac_trader_backend.controller;

import com.scu.uob.dsa.cardiac_trader_backend.service.ToolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @PostMapping("/hint")
    public ResponseEntity<Map<String, Object>> useHint(
            @RequestParam UUID sessionId,
            @RequestParam UUID stockId) {
        try {
            String hint = toolService.useHint(sessionId, stockId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", hint);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/time-boost")
    public ResponseEntity<Map<String, Object>> useTimeBoost(
            @RequestParam UUID sessionId,
            @RequestParam(defaultValue = "30") Integer secondsToAdd) {
        try {
            Integer newDuration = toolService.useTimeBoost(sessionId, secondsToAdd);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Time boost activated");
            response.put("newDuration", newDuration);
            response.put("secondsAdded", secondsToAdd);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> checkToolAvailability(
            @RequestParam UUID sessionId,
            @RequestParam String toolType) {
        Map<String, Object> response = new HashMap<>();
        try {
            com.scu.uob.dsa.cardiac_trader_backend.enums.ToolType type = 
                com.scu.uob.dsa.cardiac_trader_backend.enums.ToolType.valueOf(toolType.toUpperCase());
            boolean available = toolService.isToolAvailable(sessionId, type);
            response.put("available", available);
            response.put("toolType", type.name());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("available", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

