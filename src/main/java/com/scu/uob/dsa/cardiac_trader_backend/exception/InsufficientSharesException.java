package com.scu.uob.dsa.cardiac_trader_backend.exception;

/**
 * Exception thrown when user doesn't own enough shares to sell
 */
public class InsufficientSharesException extends RuntimeException {
    private final int owned;
    private final int requested;

    public InsufficientSharesException(int owned, int requested) {
        super(String.format("Insufficient shares. Owned: %d, Requested: %d", owned, requested));
        this.owned = owned;
        this.requested = requested;
    }

    public int getOwned() {
        return owned;
    }

    public int getRequested() {
        return requested;
    }
}

