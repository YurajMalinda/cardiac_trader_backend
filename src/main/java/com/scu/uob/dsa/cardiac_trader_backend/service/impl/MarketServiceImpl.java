package com.scu.uob.dsa.cardiac_trader_backend.service.impl;

import com.scu.uob.dsa.cardiac_trader_backend.dto.StockDTO;
import com.scu.uob.dsa.cardiac_trader_backend.enums.DifficultyLevel;
import com.scu.uob.dsa.cardiac_trader_backend.enums.StockSector;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;
import com.scu.uob.dsa.cardiac_trader_backend.model.GameSession;
import com.scu.uob.dsa.cardiac_trader_backend.model.Holding;
import com.scu.uob.dsa.cardiac_trader_backend.model.Stock;
import com.scu.uob.dsa.cardiac_trader_backend.repository.GameSessionRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.HoldingRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.StockRepository;
import com.scu.uob.dsa.cardiac_trader_backend.service.HeartAPIService;
import com.scu.uob.dsa.cardiac_trader_backend.service.MarketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MarketServiceImpl implements MarketService {

    private final StockRepository stockRepository;
    private final GameSessionRepository gameSessionRepository;
    private final HoldingRepository holdingRepository;
    private final HeartAPIService heartAPIService;

    @Value("${game.stock.count:5}")
    private Integer stockCount;

    // Pre-defined stock symbols from the concept
    private static final String[][] STOCK_DATA = {
        {"HTCH", "Heart-Tech Inc", "TECH"},
        {"CRDC", "Cardiac Systems", "MEDICAL"},
        {"PLSE", "Pulse Dynamics", "TECH"},
        {"BEAT", "HeartBeat Finance", "FINANCE"},
        {"RYTM", "Rhythm Corp", "MEDICAL"}
    };

    public MarketServiceImpl(
            StockRepository stockRepository,
            GameSessionRepository gameSessionRepository,
            HoldingRepository holdingRepository,
            HeartAPIService heartAPIService) {
        this.stockRepository = stockRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.holdingRepository = holdingRepository;
        this.heartAPIService = heartAPIService;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public List<StockDTO> initializeStocksForRound(UUID sessionId) {
        // Get game session to access difficulty level
        GameSession gameSession = gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        DifficultyLevel difficulty = gameSession.getDifficultyLevel();
        if (difficulty == null) {
            difficulty = DifficultyLevel.MEDIUM;
        }

        List<StockDTO> stockDTOs = new ArrayList<>();
        Random random = new Random();
        
        // Get variance multiplier based on difficulty
        double varianceMultiplier = getVarianceMultiplier(difficulty);

        // Use pre-defined stocks or create new ones
        for (int i = 0; i < Math.min(stockCount, STOCK_DATA.length); i++) {
            String[] stockInfo = STOCK_DATA[i];
            String symbol = stockInfo[0];
            
            // Find or create stock
            Stock stock = stockRepository.findBySymbol(symbol).orElse(null);
            
            if (stock == null) {
                stock = new Stock();
                stock.setSymbol(symbol);
                stock.setCompanyName(stockInfo[1]);
                stock.setSector(StockSector.valueOf(stockInfo[2]));
                stock = stockRepository.save(stock);
            }

            // Fetch heart puzzle from API
            try {
                HeartAPIService.HeartPuzzle puzzle = heartAPIService.fetchPuzzle();
                stock.setHeartImageUrl(puzzle.getImageUrl());
                stock.setActualHeartCount(puzzle.getCorrectAnswer());
                
                // Calculate base price
                BigDecimal basePrice = stock.calculateTruePrice();
                stock.setBasePrice(basePrice);
                
                // Set market price with variance based on difficulty
                double variance = (random.nextGaussian() * varianceMultiplier);
                BigDecimal marketPrice = basePrice.multiply(BigDecimal.valueOf(1 + variance))
                    .setScale(2, RoundingMode.HALF_UP);
                stock.setMarketPrice(marketPrice);
                
                stock = stockRepository.save(stock);
            } catch (Exception e) {
                // Log the error for debugging
                System.err.println("Error fetching heart puzzle for stock " + symbol + ": " + e.getMessage());
                e.printStackTrace();
                
                // If API fails, use random heart count for demo
                int randomHeartCount = random.nextInt(10) + 1; // 1-10 hearts
                stock.setActualHeartCount(randomHeartCount);
                stock.setBasePrice(stock.calculateTruePrice());
                
                double variance = (random.nextGaussian() * varianceMultiplier);
                BigDecimal marketPrice = stock.getBasePrice()
                    .multiply(BigDecimal.valueOf(1 + variance))
                    .setScale(2, RoundingMode.HALF_UP);
                stock.setMarketPrice(marketPrice);
                
                // Set empty heart image URL if API fails (don't set null to avoid database issues)
                stock.setHeartImageUrl("");
                stock = stockRepository.save(stock);
            }

            // Get user's holdings for this stock
            Holding holding = holdingRepository
                .findByGameSessionIdAndStockId(sessionId, stock.getId())
                .orElse(null);

            StockDTO dto = mapToDTO(stock, holding);
            stockDTOs.add(dto);
        }

        return stockDTOs;
    }

    @Override
    @SuppressWarnings("null")
    public List<StockDTO> getAvailableStocks(UUID sessionId) {
        // Verify session exists
        gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        // Get all stocks (could be filtered by active stocks for current round)
        List<Stock> stocks = stockRepository.findAll();
        
        // If no stocks exist, initialize them (stocks should be created when round starts,
        // but this handles edge cases where stocks might be missing)
        if (stocks.isEmpty()) {
            // Return empty list - stocks will be created when round starts
            // Don't auto-initialize here to preserve the design where each round gets fresh puzzles
            return new ArrayList<>();
        }
        
        return stocks.stream()
            .map(stock -> {
                Holding holding = holdingRepository
                    .findByGameSessionIdAndStockId(sessionId, stock.getId())
                    .orElse(null);
                return mapToDTO(stock, holding);
            })
            .collect(Collectors.toList());
    }

    @Override
    public void updateMarketPrices(UUID sessionId) {
        // Could implement price fluctuations here
        // For now, prices are set when stocks are initialized
    }

    @Override
    public List<StockDTO> revealTrueValues(UUID sessionId, Integer roundNumber) {
        // Get all stocks and reveal their true prices
        List<Stock> stocks = stockRepository.findAll();
        
        return stocks.stream()
            .map(stock -> {
                // Market price should now equal true price
                if (stock.getActualHeartCount() != null) {
                    stock.setMarketPrice(stock.calculateTruePrice());
                    stock = stockRepository.save(stock);
                }
                
                Holding holding = holdingRepository
                    .findByGameSessionIdAndStockId(sessionId, stock.getId())
                    .orElse(null);
                return mapToDTO(stock, holding);
            })
            .collect(Collectors.toList());
    }

    private StockDTO mapToDTO(Stock stock, Holding holding) {
        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setCompanyName(stock.getCompanyName());
        dto.setSector(stock.getSector());
        dto.setHeartImageUrl(stock.getHeartImageUrl());
        
        // Show market price (hide true price during round)
        dto.setMarketPrice(stock.getMarketPrice());
        
        // Include holdings info if user owns shares
        if (holding != null) {
            dto.setSharesOwned(holding.getShares());
            dto.setAveragePrice(holding.getAveragePrice());
            dto.setTotalValue(dto.getMarketPrice() != null ?
                dto.getMarketPrice().multiply(BigDecimal.valueOf(holding.getShares())) :
                BigDecimal.ZERO);
        } else {
            dto.setSharesOwned(0);
            dto.setTotalValue(BigDecimal.ZERO);
        }
        
        return dto;
    }
    
    /**
     * Get variance multiplier based on difficulty level
     */
    private double getVarianceMultiplier(DifficultyLevel difficulty) {
        if (difficulty == null) {
            difficulty = DifficultyLevel.MEDIUM;
        }
        
        return switch (difficulty) {
            case EASY -> 0.1;   // ±10% variance (easier to predict)
            case MEDIUM -> 0.2; // ±20% variance (default)
            case HARD -> 0.3;   // ±30% variance (harder to predict)
        };
    }
}

