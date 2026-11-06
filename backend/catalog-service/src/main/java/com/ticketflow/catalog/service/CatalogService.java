package com.ticketflow.catalog.service;

import com.ticketflow.catalog.model.*;
import com.ticketflow.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final MovieRepository movieRepository;
    private final EventRepository eventRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final ScreenSeatRepository screenSeatRepository;
    private final ShowRepository showRepository;
    private final ShowPriceTierRepository showPriceTierRepository;

    @Cacheable(value = "movies", key = "'all'")
    public List<Movie> getAllMovies() {
        log.info("Fetching all movies from PostgreSQL database");
        return movieRepository.findAll();
    }

    @Cacheable(value = "trending-movies", key = "'trending'")
    public List<Movie> getTrendingMovies() {
        log.info("Fetching trending movies from PostgreSQL database");
        return movieRepository.findByIsTrendingTrue();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public List<Movie> searchMovies(String keyword) {
        return movieRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getFeaturedEvents() {
        return eventRepository.findByIsFeaturedTrue();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Cacheable(value = "theatres", key = "#city.toLowerCase()")
    public List<Theatre> getTheatresByCity(String city) {
        log.info("Fetching theatres in {} from PostgreSQL database", city);
        return theatreRepository.findByCityIgnoreCaseAndIsActiveTrue(city);
    }

    @Cacheable(value = "shows", key = "#movieId + '-' + (#city != null ? #city.toLowerCase() : 'all') + '-' + (#date != null ? #date.toString() : 'all')")
    public List<Show> getShowsForMovie(Long movieId, String city, LocalDate date) {
        log.info("Fetching shows for movie={}, city={}, date={} from PostgreSQL database", movieId, city, date);
        if (city != null && !city.isBlank()) {
            if (date != null) {
                return showRepository.findByMovieIdAndCityIgnoreCaseAndShowDateOrderByStartTimeAsc(movieId, city, date);
            }
            return showRepository.findByMovieIdAndCityIgnoreCaseOrderByStartTimeAsc(movieId, city);
        }
        if (date != null) {
            return showRepository.findByMovieIdAndShowDate(movieId, date);
        }
        return showRepository.findByMovieId(movieId);
    }

    public Optional<Show> getShowById(Long showId) {
        return showRepository.findById(showId);
    }

    public Optional<Screen> getScreenById(Long screenId) {
        return screenRepository.findById(screenId);
    }

    public List<Screen> getScreensByTheatreId(Long theatreId) {
        return screenRepository.findByTheatreId(theatreId);
    }

    public List<ScreenSeat> getScreenSeats(Long screenId) {
        return screenSeatRepository.findByScreenIdOrderByRowNameAscColIndexAsc(screenId);
    }

    public List<ShowPriceTier> getShowPriceTiers(Long showId) {
        return showPriceTierRepository.findByShowId(showId);
    }

    @Transactional
    @CacheEvict(value = { "movies", "trending-movies" }, allEntries = true)
    public Movie saveMovie(Movie movie) {
        log.info("Saving movie and evicting movie caches: {}", movie.getTitle());
        return movieRepository.save(movie);
    }

    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public Show saveShow(Show show) {
        log.info("Saving show and evicting show cache: {} at {}", show.getMovieTitle(), show.getStartTime());
        return showRepository.save(show);
    }
}
