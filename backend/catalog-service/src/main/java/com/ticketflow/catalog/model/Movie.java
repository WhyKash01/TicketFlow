package com.ticketflow.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String genre; // Action, Sci-Fi, Thriller, Drama
    private String language; // Hindi, English, Telugu, Tamil
    private Integer durationMinutes;
    private Double rating; // e.g. 8.9
    private String certificate; // U/A, A, U
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
    private Boolean isTrending;
}

