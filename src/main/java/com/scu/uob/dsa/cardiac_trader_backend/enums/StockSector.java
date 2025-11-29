package com.scu.uob.dsa.cardiac_trader_backend.enums;

/**
 * Represents different sectors in the stock market
 */
public enum StockSector {
    TECH(1.5, "Technology"),           // Tech stocks worth more
    MEDICAL(1.3, "Medical"),           // Medical/healthcare stocks
    FINANCE(1.0, "Finance");           // Finance stocks (base multiplier)

    private final double multiplier;
    private final String displayName;

    StockSector(double multiplier, String displayName) {
        this.multiplier = multiplier;
        this.displayName = displayName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }
}

