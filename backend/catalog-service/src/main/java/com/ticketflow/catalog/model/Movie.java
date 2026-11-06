package com.ticketflow.catalog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movie_title", columnList = "title"),
        @Index(name = "idx_movie_trending", columnList = "isTrending")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 250)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String genre; // Action, Sci-Fi, Thriller, Drama

    @Column(length = 100)
    private String language; // Hindi, English, Telugu, Tamil

    private Integer durationMinutes;

    private Double rating; // e.g. 8.9

    @Column(length = 20)
    private String certificate; // U/A, A, U

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 500)
    private String backdropUrl;

    @Column(length = 500)
    private String trailerUrl;

    private LocalDate releaseDate;

    @Builder.Default
    private Boolean isTrending = false;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
