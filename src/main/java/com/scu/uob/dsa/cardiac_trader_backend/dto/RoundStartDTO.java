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
public class RoundStartDTO {
    private UUID roundId;
    private UUID gameSessionId;
    private Integer roundNumber;
    private BigDecimal capital;
    private Integer durationSeconds;
    private List<StockDTO> availableStocks;
    private Long startTime;
}

