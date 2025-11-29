package com.scu.uob.dsa.cardiac_trader_backend.repository;

import com.scu.uob.dsa.cardiac_trader_backend.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);
}

