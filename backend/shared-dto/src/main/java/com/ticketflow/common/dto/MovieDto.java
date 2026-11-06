package com.ticketflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private String language;
    private Integer durationMinutes;
    private Double rating;
    private String certificate;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
    private Boolean isTrending;
}

