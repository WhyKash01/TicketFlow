package com.ticketflow.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String category; // Comedy, Concert, Sports, Theatre
    private String performer;
    private String venue;
    private String city;
    private String bannerUrl;
    private LocalDateTime eventDateTime;
    private BigDecimal priceStartingFrom;
    private Boolean isFeatured;
}

