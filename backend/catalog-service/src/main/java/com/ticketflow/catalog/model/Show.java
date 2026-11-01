package com.ticketflow.catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long movieId;
    private Long eventId;
    private Long theatreId;
    private Long screenId;

    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String city;

    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String soundExperience; // Dolby 7.1, Dolby Atmos, IMAX Audio
    private BigDecimal basePrice;
}

