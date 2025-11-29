package com.scu.uob.dsa.cardiac_trader_backend.repository;

import com.scu.uob.dsa.cardiac_trader_backend.model.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
    List<Round> findByGameSessionId(UUID gameSessionId);
    Optional<Round> findByGameSessionIdAndRoundNumber(UUID gameSessionId, Integer roundNumber);
}

