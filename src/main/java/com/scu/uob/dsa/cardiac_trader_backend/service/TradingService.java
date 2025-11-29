package com.scu.uob.dsa.cardiac_trader_backend.service;

import com.scu.uob.dsa.cardiac_trader_backend.dto.TradeRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.TradeResponseDTO;
import com.scu.uob.dsa.cardiac_trader_backend.exception.InsufficientFundsException;
import com.scu.uob.dsa.cardiac_trader_backend.exception.InsufficientSharesException;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Service interface for stock trading operations
 */
public interface TradingService {
    /**
     * Execute a buy order
     * @param sessionId The game session ID
     * @param request Trade request with stock ID and shares
     * @return TradeResponseDTO with transaction details
     * @throws ResourceNotFoundException if stock or session not found
     * @throws InsufficientFundsException if user doesn't have enough cash
     */
    TradeResponseDTO buyStock(UUID sessionId, TradeRequestDTO request)
            throws ResourceNotFoundException, InsufficientFundsException;

    /**
     * Execute a sell order
     * @param sessionId The game session ID
     * @param request Trade request with stock ID and shares
     * @return TradeResponseDTO with transaction details
     * @throws ResourceNotFoundException if stock or session not found
     * @throws InsufficientSharesException if user doesn't own enough shares
     */
    TradeResponseDTO sellStock(UUID sessionId, TradeRequestDTO request)
            throws ResourceNotFoundException, InsufficientSharesException;

    /**
     * Get portfolio for a game session
     * @param sessionId The game session ID
     * @return PortfolioDTO with holdings and values
     * @throws ResourceNotFoundException if session not found
     */
    com.scu.uob.dsa.cardiac_trader_backend.dto.PortfolioDTO getPortfolio(UUID sessionId)
            throws ResourceNotFoundException;
}

