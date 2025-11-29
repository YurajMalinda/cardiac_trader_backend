package com.scu.uob.dsa.cardiac_trader_backend.model;

import com.scu.uob.dsa.cardiac_trader_backend.enums.ToolType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UnlockedTool entity representing tools unlocked by the player
 */
@Entity
@Table(name = "unlocked_tools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockedTool {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession gameSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "tool_type", nullable = false)
    private ToolType toolType;

    @Column(name = "unlocked_at_round", nullable = false)
    private Integer unlockedAtRound;

    @Column(name = "uses_remaining", nullable = false)
    private Integer usesRemaining = 1;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @PrePersist
    protected void onCreate() {
        unlockedAt = LocalDateTime.now();
    }
}

