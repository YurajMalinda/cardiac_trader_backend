package com.scu.uob.dsa.cardiac_trader_backend.repository;

import com.scu.uob.dsa.cardiac_trader_backend.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, UUID> {
    List<Holding> findByGameSessionId(UUID gameSessionId);
    Optional<Holding> findByGameSessionIdAndStockId(UUID gameSessionId, UUID stockId);
}

