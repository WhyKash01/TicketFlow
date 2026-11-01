package com.ticketflow.catalog.service;

import com.ticketflow.catalog.model.*;
import com.ticketflow.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
    private final ShowRepository showRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> getTrendingMovies() {
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

    public List<Theatre> getTheatresByCity(String city) {
        return theatreRepository.findByCityIgnoreCase(city);
    }

    public List<Show> getShowsForMovie(Long movieId, String city, LocalDate date) {
        if (city != null && !city.isBlank()) {
            return showRepository.findByMovieIdAndCityIgnoreCase(movieId, city);
        }
        if (date != null) {
            return showRepository.findByMovieIdAndShowDate(movieId, date);
        }
        return showRepository.findByMovieId(movieId);
    }

    public Optional<Show> getShowById(Long showId) {
        return showRepository.findById(showId);
    }

    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Show saveShow(Show show) {
        return showRepository.save(show);
    }
}

