package com.ticketflow.booking.repository;

import com.ticketflow.booking.model.Seat;
import com.ticketflow.common.enums.SeatTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByShowIdOrderByRowNameAscColIndexAsc(Long showId);
    List<Seat> findByShowIdAndRowNameOrderByColIndexAsc(Long showId, String rowName);
    List<Seat> findByShowIdAndTierOrderByRowNameAscColIndexAsc(Long showId, SeatTier tier);
}

