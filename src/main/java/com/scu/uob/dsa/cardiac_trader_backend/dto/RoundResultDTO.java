package com.scu.uob.dsa.cardiac_trader_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundResultDTO {
    private UUID roundId;
    private Integer roundNumber;
    private BigDecimal capitalAtStart;
    private BigDecimal capitalAtEnd;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    private List<StockDTO> revealedStocks;
    private List<String> unlockedTools;
    private boolean gameComplete;
    private Integer nextRoundNumber;
}

