package com.scu.uob.dsa.cardiac_trader_backend.controller;

import com.scu.uob.dsa.cardiac_trader_backend.dto.StockDTO;
import com.scu.uob.dsa.cardiac_trader_backend.service.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/stocks")
    public ResponseEntity<List<StockDTO>> getAvailableStocks(@RequestParam UUID sessionId) {
        try {
            List<StockDTO> stocks = marketService.getAvailableStocks(sessionId);
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/update-prices")
    public ResponseEntity<Void> updateMarketPrices(@RequestParam UUID sessionId) {
        try {
            marketService.updateMarketPrices(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

