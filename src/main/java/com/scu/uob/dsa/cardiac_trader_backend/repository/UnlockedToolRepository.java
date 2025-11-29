package com.scu.uob.dsa.cardiac_trader_backend.repository;

import com.scu.uob.dsa.cardiac_trader_backend.model.UnlockedTool;
import com.scu.uob.dsa.cardiac_trader_backend.enums.ToolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnlockedToolRepository extends JpaRepository<UnlockedTool, UUID> {
    List<UnlockedTool> findByGameSessionId(UUID gameSessionId);
    Optional<UnlockedTool> findByGameSessionIdAndToolType(UUID gameSessionId, ToolType toolType);
    boolean existsByGameSessionIdAndToolType(UUID gameSessionId, ToolType toolType);
}

