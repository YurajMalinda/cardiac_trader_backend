package com.scu.uob.dsa.cardiac_trader_backend.service.impl;

import com.scu.uob.dsa.cardiac_trader_backend.dto.HoldingDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.PortfolioDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.TradeRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.TradeResponseDTO;
import com.scu.uob.dsa.cardiac_trader_backend.enums.TransactionType;
import com.scu.uob.dsa.cardiac_trader_backend.exception.InsufficientFundsException;
import com.scu.uob.dsa.cardiac_trader_backend.exception.InsufficientSharesException;
import com.scu.uob.dsa.cardiac_trader_backend.exception.ResourceNotFoundException;
import com.scu.uob.dsa.cardiac_trader_backend.model.GameSession;
import com.scu.uob.dsa.cardiac_trader_backend.model.Holding;
import com.scu.uob.dsa.cardiac_trader_backend.model.Stock;
import com.scu.uob.dsa.cardiac_trader_backend.model.Transaction;
import com.scu.uob.dsa.cardiac_trader_backend.repository.GameSessionRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.HoldingRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.StockRepository;
import com.scu.uob.dsa.cardiac_trader_backend.repository.TransactionRepository;
import com.scu.uob.dsa.cardiac_trader_backend.service.TradingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TradingServiceImpl implements TradingService {

    private final GameSessionRepository gameSessionRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;

    public TradingServiceImpl(
            GameSessionRepository gameSessionRepository,
            StockRepository stockRepository,
            HoldingRepository holdingRepository,
            TransactionRepository transactionRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public TradeResponseDTO buyStock(UUID sessionId, TradeRequestDTO request)
            throws ResourceNotFoundException, InsufficientFundsException {
        
        GameSession gameSession = gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        Stock stock = stockRepository.findById(request.getStockId())  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", request.getStockId()));

        if (stock.getMarketPrice() == null) {
            throw new RuntimeException("Stock price not available");
        }

        BigDecimal totalCost = stock.getMarketPrice()
            .multiply(BigDecimal.valueOf(request.getShares()));

        // Check if user has enough cash
        BigDecimal availableCash = gameSession.getCurrentCapital() != null ? 
            gameSession.getCurrentCapital() : BigDecimal.ZERO;
        
        if (totalCost.compareTo(availableCash) > 0) {
            throw new InsufficientFundsException(
                availableCash.doubleValue(), 
                totalCost.doubleValue());
        }

        // Update game session cash
        gameSession.setCurrentCapital(availableCash.subtract(totalCost));
        gameSessionRepository.save(gameSession);

        // Update or create holding
        Holding holding = holdingRepository
            .findByGameSessionIdAndStockId(sessionId, request.getStockId())
            .orElse(null);

        if (holding == null) {
            holding = new Holding();
            holding.setGameSession(gameSession);
            holding.setStock(stock);
            holding.setShares(request.getShares());
            holding.setAveragePrice(stock.getMarketPrice());
        } else {
            // Calculate weighted average price
            BigDecimal oldTotalValue = holding.getAveragePrice()
                .multiply(BigDecimal.valueOf(holding.getShares()));
            BigDecimal newTotalValue = totalCost;
            int totalShares = holding.getShares() + request.getShares();
            
            BigDecimal newAveragePrice = oldTotalValue
                .add(newTotalValue)
                .divide(BigDecimal.valueOf(totalShares), 2, RoundingMode.HALF_UP);
            
            holding.setShares(totalShares);
            holding.setAveragePrice(newAveragePrice);
        }
        holdingRepository.save(holding);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setGameSession(gameSession);
        transaction.setStock(stock);
        transaction.setTransactionType(TransactionType.BUY);
        transaction.setShares(request.getShares());
        transaction.setPricePerShare(stock.getMarketPrice());
        transaction.setTotalValue(totalCost);
        transaction = transactionRepository.save(transaction);

        // Build response
        TradeResponseDTO response = new TradeResponseDTO();
        response.setTransactionId(transaction.getId());
        response.setStockId(stock.getId());
        response.setStockSymbol(stock.getSymbol());
        response.setTransactionType(TransactionType.BUY);
        response.setShares(request.getShares());
        response.setPricePerShare(stock.getMarketPrice());
        response.setTotalValue(totalCost);
        response.setRemainingCash(gameSession.getCurrentCapital());
        response.setTimestamp(transaction.getTimestamp());
        response.setMessage("Successfully purchased " + request.getShares() + " shares of " + stock.getSymbol());

        return response;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public TradeResponseDTO sellStock(UUID sessionId, TradeRequestDTO request)
            throws ResourceNotFoundException, InsufficientSharesException {
        
        GameSession gameSession = gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        Stock stock = stockRepository.findById(request.getStockId())  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", request.getStockId()));

        if (stock.getMarketPrice() == null) {
            throw new RuntimeException("Stock price not available");
        }

        // Find holding
        Holding holding = holdingRepository
            .findByGameSessionIdAndStockId(sessionId, request.getStockId())
            .orElseThrow(() -> new InsufficientSharesException(0, request.getShares()));

        // Check if user owns enough shares
        if (holding.getShares() < request.getShares()) {
            throw new InsufficientSharesException(
                holding.getShares(), 
                request.getShares());
        }

        BigDecimal totalRevenue = stock.getMarketPrice()
            .multiply(BigDecimal.valueOf(request.getShares()));

        // Update game session cash
        BigDecimal currentCash = gameSession.getCurrentCapital() != null ? 
            gameSession.getCurrentCapital() : BigDecimal.ZERO;
        gameSession.setCurrentCapital(currentCash.add(totalRevenue));
        gameSessionRepository.save(gameSession);

        // Update holding
        int remainingShares = holding.getShares() - request.getShares();
        if (remainingShares == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.setShares(remainingShares);
            holdingRepository.save(holding);
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setGameSession(gameSession);
        transaction.setStock(stock);
        transaction.setTransactionType(TransactionType.SELL);
        transaction.setShares(request.getShares());
        transaction.setPricePerShare(stock.getMarketPrice());
        transaction.setTotalValue(totalRevenue);
        transaction = transactionRepository.save(transaction);

        // Build response
        TradeResponseDTO response = new TradeResponseDTO();
        response.setTransactionId(transaction.getId());
        response.setStockId(stock.getId());
        response.setStockSymbol(stock.getSymbol());
        response.setTransactionType(TransactionType.SELL);
        response.setShares(request.getShares());
        response.setPricePerShare(stock.getMarketPrice());
        response.setTotalValue(totalRevenue);
        response.setRemainingCash(gameSession.getCurrentCapital());
        response.setTimestamp(transaction.getTimestamp());
        response.setMessage("Successfully sold " + request.getShares() + " shares of " + stock.getSymbol());

        return response;
    }

    @Override
    @SuppressWarnings("null")
    public PortfolioDTO getPortfolio(UUID sessionId) throws ResourceNotFoundException {
        GameSession gameSession = gameSessionRepository.findById(sessionId)  // NOSONAR - orElseThrow guarantees non-null
            .orElseThrow(() -> new ResourceNotFoundException("GameSession", "id", sessionId));

        List<Holding> holdings = holdingRepository.findByGameSessionId(sessionId);

        // Calculate cash
        BigDecimal cash = gameSession.getCurrentCapital() != null ? 
            gameSession.getCurrentCapital() : BigDecimal.ZERO;

        // Calculate stock values
        BigDecimal totalStockValue = BigDecimal.ZERO;
        List<HoldingDTO> holdingDTOs = holdings.stream()
            .map(holding -> {
                BigDecimal currentPrice = holding.getStock().getMarketPrice() != null ?
                    holding.getStock().getMarketPrice() : 
                    (holding.getStock().getActualHeartCount() != null ?
                        holding.getStock().calculateTruePrice() : BigDecimal.ZERO);
                
                BigDecimal totalValue = currentPrice.multiply(BigDecimal.valueOf(holding.getShares()));
                BigDecimal costBasis = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getShares()));
                BigDecimal profitLoss = totalValue.subtract(costBasis);
                
                BigDecimal profitLossPercentage = costBasis.compareTo(BigDecimal.ZERO) > 0 ?
                    profitLoss.divide(costBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;

                HoldingDTO dto = new HoldingDTO();
                dto.setStockId(holding.getStock().getId());
                dto.setSymbol(holding.getStock().getSymbol());
                dto.setCompanyName(holding.getStock().getCompanyName());
                dto.setShares(holding.getShares());
                dto.setAveragePrice(holding.getAveragePrice());
                dto.setCurrentPrice(currentPrice);
                dto.setTotalValue(totalValue);
                dto.setProfitLoss(profitLoss);
                dto.setProfitLossPercentage(profitLossPercentage);
                return dto;
            })
            .collect(Collectors.toList());

        // Sum total stock value
        totalStockValue = holdingDTOs.stream()
            .map(HoldingDTO::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPortfolioValue = cash.add(totalStockValue);

        PortfolioDTO portfolio = new PortfolioDTO();
        portfolio.setCash(cash);
        portfolio.setTotalStockValue(totalStockValue);
        portfolio.setTotalPortfolioValue(totalPortfolioValue);
        portfolio.setHoldings(holdingDTOs);

        return portfolio;
    }
}

