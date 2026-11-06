package com.ticketflow.catalog.repository;

import com.ticketflow.catalog.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    List<Theatre> findByCityIgnoreCase(String city);

    @Query("SELECT t FROM Theatre t WHERE LOWER(t.city) = LOWER(:city) AND (t.isActive = true OR t.isActive IS NULL)")
    List<Theatre> findByCityIgnoreCaseAndIsActiveTrue(@Param("city") String city);
}
