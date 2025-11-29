package com.scu.uob.dsa.cardiac_trader_backend.repository;

import com.scu.uob.dsa.cardiac_trader_backend.model.GameSession;
import com.scu.uob.dsa.cardiac_trader_backend.enums.GameSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
    List<GameSession> findByUserId(UUID userId);
    List<GameSession> findByUserIdAndStatus(UUID userId, GameSessionStatus status);
    Optional<GameSession> findFirstByUserIdAndStatusOrderByStartedAtDesc(UUID userId, GameSessionStatus status);
}

