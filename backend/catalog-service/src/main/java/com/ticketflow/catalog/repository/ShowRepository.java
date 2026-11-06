package com.ticketflow.catalog.repository;

import com.ticketflow.catalog.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findByMovieId(Long movieId);
    List<Show> findByMovieIdAndCityIgnoreCase(Long movieId, String city);
    List<Show> findByMovieIdAndCityIgnoreCaseOrderByStartTimeAsc(Long movieId, String city);
    List<Show> findByMovieIdAndCityIgnoreCaseAndShowDateOrderByStartTimeAsc(Long movieId, String city, LocalDate showDate);
    List<Show> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);
    List<Show> findByTheatreId(Long theatreId);
    List<Show> findByTheatreIdAndShowDateOrderByStartTimeAsc(Long theatreId, LocalDate showDate);
}
