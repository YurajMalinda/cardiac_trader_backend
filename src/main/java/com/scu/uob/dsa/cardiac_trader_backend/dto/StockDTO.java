package com.scu.uob.dsa.cardiac_trader_backend.dto;

import com.scu.uob.dsa.cardiac_trader_backend.enums.StockSector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {
    private UUID id;
    private String symbol;
    private String companyName;
    private StockSector sector;
    private String heartImageUrl;
    private BigDecimal marketPrice;
    private Integer sharesOwned;
    private BigDecimal averagePrice;
    private BigDecimal totalValue;
}

