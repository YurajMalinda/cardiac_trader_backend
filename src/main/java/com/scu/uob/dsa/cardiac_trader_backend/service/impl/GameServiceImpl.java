package com.scu.uob.dsa.cardiac_trader_backend.service.impl;

import com.scu.uob.dsa.cardiac_trader_backend.dto.GameSessionDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RoundResultDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.RoundStartDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.StockDTO;
import com.scu.uob.dsa.cardiac_trader_backend.enums.DifficultyLevel;
import com.scu.uob.dsa.cardiac_trader_backend.enums.GameSessionStatus;
import com.scu.uob.dsa.cardiac_trader_backend.enums.RoundStatus;
import com.scu.uob.dsa.cardiac_trader_backend.enums.ToolType;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;
import com.scu.uob.dsa.cardiac_trader_backend.model.GameSession;
import com.scu.uob.dsa.cardiac_trader_backend.model.Round;
import com.scu.uob.dsa.cardiac_trader_backend.model.User;
import com.scu.uob.dsa.cardiac_trader_backend.repository.GameSessionRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.RoundRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.UserRepository;
import com.scu.uob.dsa.cardiac_trader_backend.service.GameService;
import com.scu.uob.dsa.cardiac_trader_backend.service.MarketService;
import com.scu.uob.dsa.cardiac_trader_backend.service.ToolService;
import com.scu.uob.dsa.cardiac_trader_backend.service.TradingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final GameSessionRepository gameSessionRepository;
    private final RoundRepository roundRepository;
    private final UserRepository userRepository;
    private final MarketService marketService;
    private final ToolService toolService;
    private final TradingService tradingService;
    
    @Value("${game.starting.capital:10000}")
    private BigDecimal startingCapital;
    
    @Value("${game.total.rounds:3}")
    private Integer totalRounds;

    @Value("${game.profit.threshold.hint:500}")
    private BigDecimal hintThreshold;

    @Value("${game.profit.threshold.timeboost:1000}")
    private BigDecimal timeBoostThreshold;

    public GameServiceImpl(
            GameSessionRepository gameSessionRepository,
            RoundRepository roundRepository,
            UserRepository userRepository,
            MarketService marketService,
            ToolService toolService,
            TradingService tradingService) {
        this.gameSessionRepository = gameSessionRepository;
        this.roundRepository = roundRepository;
        this.userRepository = userRepository;
        this.marketService = marketService;
        this.toolService = toolService;
        this.tradingService = tradingService;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public GameSessionDTO startNewGame(UUID userId, DifficultyLevel difficultyLevel) {
        // Get user entity
        User user = userRepository.findById(userId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Close any existing active sessions
        List<GameSession> activeSessions = gameSessionRepository
            .findByUserIdAndStatus(userId, GameSessionStatus.ACTIVE);
        
        for (GameSession session : activeSessions) {
            session.setStatus(GameSessionStatus.ABANDONED);
            session.setCompletedAt(LocalDateTime.now());
            gameSessionRepository.save(session);
        }

        // Use default if null
        if (difficultyLevel == null) {
            difficultyLevel = DifficultyLevel.MEDIUM;
        }

        // Create new game session
        GameSession gameSession = new GameSession();
        gameSession.setUser(user);
        gameSession.setStartingCapital(startingCapital);
        gameSession.setCurrentCapital(startingCapital);
        gameSession.setCurrentRound(1);
        gameSession.setStatus(GameSessionStatus.ACTIVE);
        gameSession.setDifficultyLevel(difficultyLevel);

        gameSession = gameSessionRepository.save(gameSession);

        return mapToDTO(gameSession);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public RoundStartDTO startRound(UUID sessionId) {
        GameSession gameSession = gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        if (gameSession.getCurrentRound() > totalRounds) {
            throw new RuntimeException("All rounds completed");
        }

        // Initialize stocks for this round
        List<StockDTO> stocks = marketService.initializeStocksForRound(sessionId);

        // Create round
        Round round = new Round();
        round.setGameSession(gameSession);
        round.setRoundNumber(gameSession.getCurrentRound());
        round.setCapitalAtStart(gameSession.getCurrentCapital());
        round.setStatus(RoundStatus.ACTIVE);
        round = roundRepository.save(round);

        // Get difficulty-specific round duration
        int roundDuration = getRoundDuration(gameSession.getDifficultyLevel());
        
        RoundStartDTO dto = new RoundStartDTO();
        dto.setRoundId(round.getId());
        dto.setGameSessionId(sessionId);
        dto.setRoundNumber(round.getRoundNumber());
        dto.setCapital(gameSession.getCurrentCapital());
        dto.setDurationSeconds(roundDuration);
        dto.setAvailableStocks(stocks);
        dto.setStartTime(System.currentTimeMillis());

        return dto;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public RoundResultDTO completeRound(UUID sessionId, Integer roundNumber) {
        GameSession gameSession = gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        Round round = roundRepository
            .findByGameSessionIdAndRoundNumber(sessionId, roundNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Round", "roundNumber", roundNumber));

        // Reveal true values
        List<StockDTO> revealedStocks = marketService.revealTrueValues(sessionId, roundNumber);

        // Calculate final portfolio value using TradingService
        com.scu.uob.dsa.cardiac_trader_backend.dto.PortfolioDTO portfolio = tradingService.getPortfolio(sessionId);
        BigDecimal capitalAtEnd = portfolio.getTotalPortfolioValue();
        BigDecimal profitLoss = capitalAtEnd.subtract(round.getCapitalAtStart());
        
        round.setCapitalAtEnd(capitalAtEnd);
        round.setProfitLoss(profitLoss);
        round.setStatus(RoundStatus.COMPLETED);
        round.setCompletedAt(LocalDateTime.now());
        
        // Calculate duration if started_at is available
        if (round.getStartedAt() != null) {
            long duration = java.time.Duration.between(round.getStartedAt(), LocalDateTime.now()).getSeconds();
            round.setDurationSeconds((int) duration);
        }
        
        roundRepository.save(round);

        // Update game session
        gameSession.setCurrentCapital(capitalAtEnd);
        if (roundNumber >= totalRounds) {
            gameSession.setStatus(GameSessionStatus.COMPLETED);
            gameSession.setCompletedAt(LocalDateTime.now());
        } else {
            gameSession.setCurrentRound(roundNumber + 1);
        }
        gameSessionRepository.save(gameSession);

        // Check for tool unlocks based on difficulty
        List<String> unlockedTools = new java.util.ArrayList<>();
        DifficultyLevel difficulty = gameSession.getDifficultyLevel();
        BigDecimal difficultyHintThreshold = getHintThreshold(difficulty);
        BigDecimal difficultyTimeBoostThreshold = getTimeBoostThreshold(difficulty);
        
        if (profitLoss.compareTo(difficultyHintThreshold) >= 0) {
            toolService.unlockTool(sessionId, ToolType.HINT, roundNumber);
            unlockedTools.add("HINT");
        }
        if (profitLoss.compareTo(difficultyTimeBoostThreshold) >= 0) {
            toolService.unlockTool(sessionId, ToolType.TIME_BOOST, roundNumber);
            unlockedTools.add("TIME_BOOST");
        }

        RoundResultDTO dto = new RoundResultDTO();
        dto.setRoundId(round.getId());
        dto.setRoundNumber(roundNumber);
        dto.setCapitalAtStart(round.getCapitalAtStart());
        dto.setCapitalAtEnd(capitalAtEnd);
        dto.setProfitLoss(profitLoss);
        
        // Calculate profit loss percentage
        if (round.getCapitalAtStart().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = profitLoss
                .divide(round.getCapitalAtStart(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            dto.setProfitLossPercentage(percentage);
        } else {
            dto.setProfitLossPercentage(BigDecimal.ZERO);
        }
        
        dto.setRevealedStocks(revealedStocks);
        dto.setUnlockedTools(unlockedTools);
        dto.setGameComplete(roundNumber >= totalRounds);
        dto.setNextRoundNumber(roundNumber < totalRounds ? roundNumber + 1 : null);

        return dto;
    }

    @Override
    public GameSessionDTO getCurrentSession(UUID userId) {
        return gameSessionRepository
            .findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, GameSessionStatus.ACTIVE)
            .map(this::mapToDTO)
            .orElse(null);
    }

    @Override
    @Transactional
    public void abandonActiveSessions(UUID userId) {
        // Find all active sessions for the user
        List<GameSession> activeSessions = gameSessionRepository
            .findByUserIdAndStatus(userId, GameSessionStatus.ACTIVE);
        
        // Mark all active sessions as abandoned
        for (GameSession session : activeSessions) {
            session.setStatus(GameSessionStatus.ABANDONED);
            session.setCompletedAt(LocalDateTime.now());
            gameSessionRepository.save(session);
        }
    }

    private GameSessionDTO mapToDTO(GameSession gameSession) {
        GameSessionDTO dto = new GameSessionDTO();
        dto.setId(gameSession.getId());
        dto.setUserId(gameSession.getUser().getId());
        dto.setCurrentRound(gameSession.getCurrentRound());
        dto.setStartingCapital(gameSession.getStartingCapital());
        dto.setCurrentCapital(gameSession.getCurrentCapital());
        dto.setStatus(gameSession.getStatus());
        dto.setDifficultyLevel(gameSession.getDifficultyLevel());
        
        // Check if there's an active round
        boolean hasActiveRound = roundRepository.findByGameSessionId(gameSession.getId())
            .stream()
            .anyMatch(r -> r.getStatus() == RoundStatus.ACTIVE);
        dto.setHasActiveRound(hasActiveRound);
        
        return dto;
    }
    
    /**
     * Get round duration based on difficulty level
     */
    private int getRoundDuration(DifficultyLevel difficulty) {
        if (difficulty == null) {
            difficulty = DifficultyLevel.MEDIUM;
        }
        
        return switch (difficulty) {
            case EASY -> 90;   // 90 seconds
            case MEDIUM -> 60; // 60 seconds (default)
            case HARD -> 45;   // 45 seconds
        };
    }
    
    /**
     * Get profit threshold for hint based on difficulty
     */
    public BigDecimal getHintThreshold(DifficultyLevel difficulty) {
        if (difficulty == null) {
            difficulty = DifficultyLevel.MEDIUM;
        }
        
        return switch (difficulty) {
            case EASY -> hintThreshold.multiply(BigDecimal.valueOf(0.7));   // $350
            case MEDIUM -> hintThreshold;                                    // $500
            case HARD -> hintThreshold.multiply(BigDecimal.valueOf(1.5));   // $750
        };
    }
    
    /**
     * Get profit threshold for time boost based on difficulty
     */
    public BigDecimal getTimeBoostThreshold(DifficultyLevel difficulty) {
        if (difficulty == null) {
            difficulty = DifficultyLevel.MEDIUM;
        }
        
        return switch (difficulty) {
            case EASY -> timeBoostThreshold.multiply(BigDecimal.valueOf(0.7));   // $700
            case MEDIUM -> timeBoostThreshold;                                     // $1000
            case HARD -> timeBoostThreshold.multiply(BigDecimal.valueOf(1.5));     // $1500
        };
    }
}

