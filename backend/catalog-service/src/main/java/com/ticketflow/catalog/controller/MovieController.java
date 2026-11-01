package com.ticketflow.catalog.controller;

import com.ticketflow.catalog.model.Movie;
import com.ticketflow.catalog.service.CatalogService;
import com.ticketflow.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MovieController {
    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Movie>>> getAllMovies() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllMovies(), "Fetched all movies successfully"));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Movie>>> getTrendingMovies() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getTrendingMovies(), "Fetched trending movies"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> getMovieById(@PathVariable Long id) {
        return catalogService.getMovieById(id)
                .map(movie -> ResponseEntity.ok(ApiResponse.success(movie, "Movie found")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Movie not found with id: " + id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Movie>>> searchMovies(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(catalogService.searchMovies(query), "Search results"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Movie>> createMovie(@RequestBody Movie movie) {
        Movie saved = catalogService.saveMovie(movie);
        return ResponseEntity.ok(ApiResponse.success(saved, "Movie created successfully"));
    }
}

