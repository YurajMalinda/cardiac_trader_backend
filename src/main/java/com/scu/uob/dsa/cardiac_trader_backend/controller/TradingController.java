package com.scu.uob.dsa.cardiac_trader_backend.controller;

import com.scu.uob.dsa.cardiac_trader_backend.dto.PortfolioDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.TradeRequestDTO;
import com.scu.uob.dsa.cardiac_trader_backend.dto.TradeResponseDTO;
import com.scu.uob.dsa.cardiac_trader_backend.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("/buy")
    public ResponseEntity<TradeResponseDTO> buyStock(
            @RequestParam UUID sessionId,
            @Valid @RequestBody TradeRequestDTO request) {
        try {
            TradeResponseDTO response = tradingService.buyStock(sessionId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponseDTO> sellStock(
            @RequestParam UUID sessionId,
            @Valid @RequestBody TradeRequestDTO request) {
        try {
            TradeResponseDTO response = tradingService.sellStock(sessionId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioDTO> getPortfolio(@RequestParam UUID sessionId) {
        try {
            PortfolioDTO portfolio = tradingService.getPortfolio(sessionId);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

