package com.scu.uob.dsa.cardiac_trader_backend.dto;

import com.scu.uob.dsa.cardiac_trader_backend.enums.DifficultyLevel;
import com.scu.uob.dsa.cardiac_trader_backend.enums.GameSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionDTO {
    private UUID id;
    private UUID userId;
    private Integer currentRound;
    private BigDecimal startingCapital;
    private BigDecimal currentCapital;
    private GameSessionStatus status;
    private DifficultyLevel difficultyLevel;
    private boolean hasActiveRound;
}

