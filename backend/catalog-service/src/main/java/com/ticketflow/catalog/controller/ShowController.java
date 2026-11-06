package com.ticketflow.catalog.controller;

import com.ticketflow.catalog.model.Show;
import com.ticketflow.catalog.model.ShowPriceTier;
import com.ticketflow.catalog.service.CatalogService;
import com.ticketflow.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/shows")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShowController {
    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Show>>> getShows(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (movieId != null) {
            List<Show> shows = catalogService.getShowsForMovie(movieId, city, date);
            return ResponseEntity.ok(ApiResponse.success(shows, "Shows fetched for movie"));
        }
        return ResponseEntity.ok(ApiResponse.success(List.of(), "Please provide a movieId to list shows"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Show>> getShowById(@PathVariable Long id) {
        return catalogService.getShowById(id)
                .map(show -> ResponseEntity.ok(ApiResponse.success(show, "Show found")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Show not found with id: " + id)));
    }

    @GetMapping("/{id}/price-tiers")
    public ResponseEntity<ApiResponse<List<ShowPriceTier>>> getShowPriceTiers(@PathVariable Long id) {
        List<ShowPriceTier> tiers = catalogService.getShowPriceTiers(id);
        return ResponseEntity.ok(ApiResponse.success(tiers, "Price tiers fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Show>> createShow(@RequestBody Show show) {
        Show saved = catalogService.saveShow(show);
        return ResponseEntity.ok(ApiResponse.success(saved, "Show created successfully"));
    }
}

