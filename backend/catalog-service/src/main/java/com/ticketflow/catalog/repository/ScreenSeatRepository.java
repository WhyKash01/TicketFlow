package com.ticketflow.catalog.repository;

import com.ticketflow.catalog.model.ScreenSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenSeatRepository extends JpaRepository<ScreenSeat, Long> {
    List<ScreenSeat> findByScreenIdOrderByRowNameAscColIndexAsc(Long screenId);
}

