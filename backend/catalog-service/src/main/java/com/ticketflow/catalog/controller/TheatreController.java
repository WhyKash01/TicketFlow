package com.ticketflow.catalog.controller;

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
}

