package com.ticketflow.catalog.controller;

import com.ticketflow.catalog.model.Screen;
import com.ticketflow.catalog.model.ScreenSeat;
import com.ticketflow.catalog.model.Theatre;
import com.ticketflow.catalog.service.CatalogService;
import com.ticketflow.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/theatres")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TheatreController {
    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Theatre>>> getTheatres(@RequestParam(required = false) String city) {
        List<Theatre> theatres = (city != null && !city.isBlank())
                ? catalogService.getTheatresByCity(city)
                : catalogService.getTheatresByCity("Mumbai");
        return ResponseEntity.ok(ApiResponse.success(theatres, "Theatres fetched successfully"));
    }

    @GetMapping("/{theatreId}/screens")
    public ResponseEntity<ApiResponse<List<Screen>>> getScreensByTheatre(@PathVariable Long theatreId) {
        List<Screen> screens = catalogService.getScreensByTheatreId(theatreId);
        return ResponseEntity.ok(ApiResponse.success(screens, "Screens fetched successfully"));
    }

    @GetMapping("/screens/{screenId}")
    public ResponseEntity<ApiResponse<Screen>> getScreenById(@PathVariable Long screenId) {
        return catalogService.getScreenById(screenId)
                .map(screen -> ResponseEntity.ok(ApiResponse.success(screen, "Screen details fetched")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Screen not found with id: " + screenId)));
    }

    @GetMapping("/screens/{screenId}/seats")
    public ResponseEntity<ApiResponse<List<ScreenSeat>>> getScreenSeats(@PathVariable Long screenId) {
        List<ScreenSeat> seats = catalogService.getScreenSeats(screenId);
        return ResponseEntity.ok(ApiResponse.success(seats, "Screen seats layout fetched successfully"));
    }
}
