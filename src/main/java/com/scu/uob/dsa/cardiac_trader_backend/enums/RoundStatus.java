package com.scu.uob.dsa.cardiac_trader_backend.enums;

/**
 * Represents the status of a game round
 */
public enum RoundStatus {
    WAITING,      // Round not started yet
    ACTIVE,       // Round currently in progress
    COMPLETED,    // Round finished
    ABANDONED     // Round abandoned by user
}

