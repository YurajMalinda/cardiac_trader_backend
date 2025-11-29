package com.scu.uob.dsa.cardiac_trader_backend.dto;

import com.scu.uob.dsa.cardiac_trader_backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponseDTO {
    private UUID transactionId;
    private UUID stockId;
    private String stockSymbol;
    private TransactionType transactionType;
    private Integer shares;
    private BigDecimal pricePerShare;
    private BigDecimal totalValue;
    private BigDecimal remainingCash;
    private LocalDateTime timestamp;
    private String message;
}

