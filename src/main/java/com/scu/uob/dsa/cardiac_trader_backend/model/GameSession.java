package com.scu.uob.dsa.cardiac_trader_backend.model;

import com.scu.uob.dsa.cardiac_trader_backend.enums.DifficultyLevel;
import com.scu.uob.dsa.cardiac_trader_backend.enums.GameSessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GameSession entity representing a complete 3-round game
 */
@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "current_round", nullable = false)
    private Integer currentRound = 1;

    @Column(name = "starting_capital", precision = 10, scale = 2, nullable = false)
    private BigDecimal startingCapital;

    @Column(name = "current_capital", precision = 10, scale = 2)
    private BigDecimal currentCapital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSessionStatus status = GameSessionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Round> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Holding> holdings = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UnlockedTool> unlockedTools = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (currentCapital == null) {
            currentCapital = startingCapital;
        }
    }
}

