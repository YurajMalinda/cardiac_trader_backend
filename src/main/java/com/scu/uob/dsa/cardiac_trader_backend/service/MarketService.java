package com.scu.uob.dsa.cardiac_trader_backend.service;

import com.scu.uob.dsa.cardiac_trader_backend.dto.StockDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for market data operations
 */
public interface MarketService {
    /**
     * Get all available stocks for the current round
     * @param sessionId The game session ID
     * @return List of StockDTOs
     */
    List<StockDTO> getAvailableStocks(UUID sessionId);

    /**
     * Initialize stocks for a new round
     * @param sessionId The game session ID
     * @return List of StockDTOs with heart puzzles loaded
     */
    List<StockDTO> initializeStocksForRound(UUID sessionId);

    /**
     * Update market prices (can be called periodically)
     * @param sessionId The game session ID
     */
    void updateMarketPrices(UUID sessionId);

    /**
     * Reveal true heart counts for all stocks in a round
     * @param sessionId The game session ID
     * @param roundNumber The round number
     * @return List of StockDTOs with revealed true values
     */
    List<StockDTO> revealTrueValues(UUID sessionId, Integer roundNumber);
}

