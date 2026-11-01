package com.ticketflow.catalog.controller;

import com.ticketflow.catalog.model.Event;
import com.ticketflow.catalog.service.CatalogService;
import com.ticketflow.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventController {
    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getAllEvents(), "Fetched all events"));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<Event>>> getFeaturedEvents() {
        return ResponseEntity.ok(ApiResponse.success(catalogService.getFeaturedEvents(), "Fetched featured events"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> getEventById(@PathVariable Long id) {
        return catalogService.getEventById(id)
                .map(event -> ResponseEntity.ok(ApiResponse.success(event, "Event found")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Event not found with id: " + id)));
    }
}

