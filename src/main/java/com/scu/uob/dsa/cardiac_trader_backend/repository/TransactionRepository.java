package com.scu.uob.dsa.cardiac_trader_backend.repository;

import com.scu.uob.dsa.cardiac_trader_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByGameSessionId(UUID gameSessionId);
    List<Transaction> findByGameSessionIdAndRoundId(UUID gameSessionId, UUID roundId);
    List<Transaction> findByStockId(UUID stockId);
}

