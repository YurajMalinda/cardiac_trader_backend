package com.scu.uob.dsa.cardiac_trader_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Holding entity representing a user's stock ownership in a game session
 */
@Entity
@Table(name = "holdings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession gameSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Integer shares;

    @Column(name = "average_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal averagePrice;

    /**
     * Calculate total value of this holding
     */
    public BigDecimal getTotalValue() {
        if (stock != null && stock.getMarketPrice() != null) {
            return stock.getMarketPrice().multiply(BigDecimal.valueOf(shares));
        }
        return BigDecimal.ZERO;
    }
}

