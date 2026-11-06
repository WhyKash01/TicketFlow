package com.ticketflow.catalog.repository;

import com.ticketflow.catalog.model.ShowPriceTier;
import com.ticketflow.common.enums.SeatTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowPriceTierRepository extends JpaRepository<ShowPriceTier, Long> {
    List<ShowPriceTier> findByShowId(Long showId);
    Optional<ShowPriceTier> findByShowIdAndTier(Long showId, SeatTier tier);
}

