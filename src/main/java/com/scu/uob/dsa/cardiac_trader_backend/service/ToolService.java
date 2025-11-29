package com.scu.uob.dsa.cardiac_trader_backend.service;

import com.scu.uob.dsa.cardiac_trader_backend.enums.ToolType;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Service interface for tool management
 */
public interface ToolService {
    /**
     * Unlock a tool for a game session
     * @param sessionId The game session ID
     * @param toolType The type of tool to unlock
     * @param roundNumber The round number when unlocked
     */
    void unlockTool(UUID sessionId, ToolType toolType, Integer roundNumber);

    /**
     * Use a hint tool to reveal partial heart count
     * @param sessionId The game session ID
     * @param stockId The stock ID to get hint for
     * @return Partial heart count hint (e.g., "between 5-8")
     * @throws ResourceNotFoundException if tool not available or stock not found
     */
    String useHint(UUID sessionId, UUID stockId) throws ResourceNotFoundException;

    /**
     * Use time boost tool to extend round timer
     * @param sessionId The game session ID
     * @param secondsToAdd Seconds to add to timer
     * @return New timer duration in seconds
     * @throws ResourceNotFoundException if tool not available
     */
    Integer useTimeBoost(UUID sessionId, Integer secondsToAdd) throws ResourceNotFoundException;

    /**
     * Check if a tool is unlocked for a session
     * @param sessionId The game session ID
     * @param toolType The type of tool
     * @return True if tool is unlocked and has uses remaining
     */
    boolean isToolAvailable(UUID sessionId, ToolType toolType);
}

