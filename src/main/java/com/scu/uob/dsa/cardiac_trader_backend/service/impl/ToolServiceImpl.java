package com.scu.uob.dsa.cardiac_trader_backend.service.impl;

import com.scu.uob.dsa.cardiac_trader_backend.enums.ToolType;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;
import com.scu.uob.dsa.cardiac_trader_backend.model.GameSession;
import com.scu.uob.dsa.cardiac_trader_backend.model.Stock;
import com.scu.uob.dsa.cardiac_trader_backend.model.UnlockedTool;
import com.scu.uob.dsa.cardiac_trader_backend.repository.GameSessionRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.StockRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.UnlockedToolRepository;
import com.scu.uob.dsa.cardiac_trader_backend.service.ToolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ToolServiceImpl implements ToolService {

    private final UnlockedToolRepository unlockedToolRepository;
    private final GameSessionRepository gameSessionRepository;
    private final StockRepository stockRepository;

    public ToolServiceImpl(
            UnlockedToolRepository unlockedToolRepository,
            GameSessionRepository gameSessionRepository,
            StockRepository stockRepository) {
        this.unlockedToolRepository = unlockedToolRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void unlockTool(UUID sessionId, ToolType toolType, Integer roundNumber) {
        GameSession gameSession = gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        // Check if tool already unlocked
        if (unlockedToolRepository.existsByGameSessionIdAndToolType(sessionId, toolType)) {
            // Update uses remaining
            UnlockedTool existing = unlockedToolRepository
                .findByGameSessionIdAndToolType(sessionId, toolType)
                .orElse(null);
            if (existing != null) {
                existing.setUsesRemaining(existing.getUsesRemaining() + 1);
                unlockedToolRepository.save(existing);
            }
        } else {
            // Create new unlocked tool
            UnlockedTool tool = new UnlockedTool();
            tool.setGameSession(gameSession);
            tool.setToolType(toolType);
            tool.setUnlockedAtRound(roundNumber);
            tool.setUsesRemaining(1);
            unlockedToolRepository.save(tool);
        }
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public String useHint(UUID sessionId, UUID stockId) throws ResourceNotFoundException {
        if (!isToolAvailable(sessionId, ToolType.HINT)) {
            throw new ResourceNotFoundException("Tool", "type", ToolType.HINT);
        }

        Stock stock = stockRepository.findById(stockId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", stockId));

        if (stock.getActualHeartCount() == null) {
            throw new RuntimeException("Heart count not available for this stock");
        }

        // Provide a hint (range around actual count)
        int actualCount = stock.getActualHeartCount();
        int lowerBound = Math.max(1, actualCount - 2);
        int upperBound = actualCount + 2;

        // Consume one use
        UnlockedTool tool = unlockedToolRepository
            .findByGameSessionIdAndToolType(sessionId, ToolType.HINT)
            .orElse(null);
        
        if (tool != null && tool.getUsesRemaining() > 0) {
            tool.setUsesRemaining(tool.getUsesRemaining() - 1);
            if (tool.getUsesRemaining() == 0) {
                unlockedToolRepository.delete(tool);
            } else {
                unlockedToolRepository.save(tool);
            }
        }

        return String.format("The heart count is between %d and %d", lowerBound, upperBound);
    }

    @Override
    @Transactional
    public Integer useTimeBoost(UUID sessionId, Integer secondsToAdd) throws ResourceNotFoundException {
        if (!isToolAvailable(sessionId, ToolType.TIME_BOOST)) {
            throw new ResourceNotFoundException("Tool", "type", ToolType.TIME_BOOST);
        }

        // Consume one use
        UnlockedTool tool = unlockedToolRepository
            .findByGameSessionIdAndToolType(sessionId, ToolType.TIME_BOOST)
            .orElse(null);
        
        if (tool != null && tool.getUsesRemaining() > 0) {
            tool.setUsesRemaining(tool.getUsesRemaining() - 1);
            if (tool.getUsesRemaining() == 0) {
                unlockedToolRepository.delete(tool);
            } else {
                unlockedToolRepository.save(tool);
            }
        }

        // Return new duration (default 60 + added seconds)
        return 60 + secondsToAdd;
    }

    @Override
    public boolean isToolAvailable(UUID sessionId, ToolType toolType) {
        UnlockedTool tool = unlockedToolRepository
            .findByGameSessionIdAndToolType(sessionId, toolType)
            .orElse(null);
        
        return tool != null && tool.getUsesRemaining() > 0;
    }
}

