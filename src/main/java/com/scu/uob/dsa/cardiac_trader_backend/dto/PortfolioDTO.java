package com.scu.uob.dsa.cardiac_trader_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDTO {
    private BigDecimal cash;
    private BigDecimal totalStockValue;
    private BigDecimal totalPortfolioValue;
    private List<HoldingDTO> holdings;
}

