package com.scu.uob.dsa.cardiac_trader_backend.service;

import com.scu.uob.dsa.cardiac_trader_backend.dto.GameSessionDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RoundResultDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RoundStartDTO;
import com.scu.uob.dsa.cardiac_trader_backend.enums.DifficultyLevel;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Service interface for game management
 */
public interface GameService {
    /**
     * Start a new game session for a user
     * @param userId The user ID
     * @param difficultyLevel The difficulty level (EASY, MEDIUM, HARD)
     * @return GameSessionDTO of the created session
     */
    GameSessionDTO startNewGame(UUID userId, DifficultyLevel difficultyLevel);

    /**
     * Start a new round in an active game session
     * @param sessionId The game session ID
     * @return RoundStartDTO with round details and available stocks
     * @throws ResourceNotFoundException if session not found
     */
    RoundStartDTO startRound(UUID sessionId) throws ResourceNotFoundException;

    /**
     * Complete a round and calculate results
     * @param sessionId The game session ID
     * @param roundNumber The round number
     * @return RoundResultDTO with profit/loss and unlocked tools
     * @throws ResourceNotFoundException if round not found
     */
    RoundResultDTO completeRound(UUID sessionId, Integer roundNumber) throws ResourceNotFoundException;

    /**
     * Get current game session for a user
     * @param userId The user ID
     * @return GameSessionDTO or null if no active session
     */
    GameSessionDTO getCurrentSession(UUID userId);

    /**
     * Abandon all active game sessions for a user (e.g., on logout)
     * @param userId The user ID
     */
    void abandonActiveSessions(UUID userId);
}

