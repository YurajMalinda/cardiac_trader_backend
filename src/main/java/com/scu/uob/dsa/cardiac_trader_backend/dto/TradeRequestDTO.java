package com.scu.uob.dsa.cardiac_trader_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequestDTO {
    @NotNull(message = "Stock ID is required")
    private UUID stockId;

    @NotNull(message = "Shares is required")
    @Min(value = 1, message = "Shares must be at least 1")
    private Integer shares;
}

