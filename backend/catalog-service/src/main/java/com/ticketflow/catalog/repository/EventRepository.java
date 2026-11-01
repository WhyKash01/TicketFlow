package com.ticketflow.catalog.repository;

import com.ticketflow.catalog.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCityIgnoreCase(String city);
    List<Event> findByIsFeaturedTrue();
    List<Event> findByCategoryIgnoreCase(String category);
}

