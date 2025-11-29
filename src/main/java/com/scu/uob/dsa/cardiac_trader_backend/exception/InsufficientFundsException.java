package com.scu.uob.dsa.cardiac_trader_backend.exception;

/**
 * Exception thrown when user doesn't have enough funds for a transaction
 */
public class InsufficientFundsException extends RuntimeException {
    private final double available;
    private final double required;

    public InsufficientFundsException(double available, double required) {
        super(String.format("Insufficient funds. Available: $%.2f, Required: $%.2f", available, required));
        this.available = available;
        this.required = required;
    }

    public double getAvailable() {
        return available;
    }

    public double getRequired() {
        return required;
    }
}

