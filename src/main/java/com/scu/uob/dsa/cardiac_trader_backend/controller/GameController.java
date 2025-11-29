package com.scu.uob.dsa.cardiac_trader_backend.controller;

import com.scu.uob.dsa.cardiac_trader_backend.dto.GameSessionDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RoundResultDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RoundStartDTO;
import com.scu.uob.dsa.cardiac_trader_backend.enums.DifficultyLevel;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ExternalAPIException;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;
import com.scu.uob.dsa.cardiac_trader_backend.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<GameSessionDTO> startNewGame(
            @RequestParam UUID userId,
            @RequestParam(required = false, defaultValue = "MEDIUM") String difficulty) {
        try {
            // Parse difficulty level, default to MEDIUM if invalid
            DifficultyLevel difficultyLevel;
            try {
                difficultyLevel = DifficultyLevel.valueOf(difficulty.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid difficulty level: {}, defaulting to MEDIUM", difficulty);
                difficultyLevel = DifficultyLevel.MEDIUM;
            }
            
            GameSessionDTO session = gameService.startNewGame(userId, difficultyLevel);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            logger.error("Error starting new game for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/round/start")
    public ResponseEntity<RoundStartDTO> startRound(@RequestParam UUID sessionId) {
        try {
            logger.info("Starting round for sessionId: {}", sessionId);
            RoundStartDTO round = gameService.startRound(sessionId);
            logger.info("Round started successfully: roundId={}, roundNumber={}", round.getRoundId(), round.getRoundNumber());
            return ResponseEntity.ok(round);
        } catch (ResourceNotFoundException e) {
            logger.error("GameSession not found for sessionId: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ExternalAPIException e) {
            logger.error("External API error when starting round for sessionId: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error starting round for sessionId: {}", sessionId, e);
            logger.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                logger.error("Cause: {}, Cause message: {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/round/complete")
    public ResponseEntity<RoundResultDTO> completeRound(
            @RequestParam UUID sessionId,
            @RequestParam Integer roundNumber) {
        try {
            RoundResultDTO result = gameService.completeRound(sessionId, roundNumber);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/session")
    public ResponseEntity<GameSessionDTO> getCurrentSession(@RequestParam UUID userId) {
        GameSessionDTO session = gameService.getCurrentSession(userId);
        if (session != null) {
            return ResponseEntity.ok(session);
        }
        return ResponseEntity.notFound().build();
    }
}

