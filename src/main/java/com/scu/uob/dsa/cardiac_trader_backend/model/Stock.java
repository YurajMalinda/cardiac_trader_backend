package com.scu.uob.dsa.cardiac_trader_backend.model;

import com.scu.uob.dsa.cardiac_trader_backend.enums.StockSector;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock entity representing a company stock in the game
 */
@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 10)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockSector sector;

    @Column(name = "heart_image_url", columnDefinition = "LONGTEXT")
    private String heartImageUrl;

    @Column(name = "actual_heart_count")
    private Integer actualHeartCount;  // Hidden until market closes

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "market_price", precision = 10, scale = 2)
    private BigDecimal marketPrice;  // Current market price (may differ from true value)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Calculate the true price based on heart count and sector multiplier
     */
    public BigDecimal calculateTruePrice() {
        if (actualHeartCount == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(actualHeartCount * 100)
                .multiply(BigDecimal.valueOf(sector.getMultiplier()));
    }
}

