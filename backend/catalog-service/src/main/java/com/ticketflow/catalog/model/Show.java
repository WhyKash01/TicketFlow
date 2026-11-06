package com.ticketflow.catalog.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ticketflow.common.enums.ScreenFormat;
import com.ticketflow.common.enums.ShowStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shows", indexes = {
        @Index(name = "idx_show_movie_city", columnList = "movieId, city"),
        @Index(name = "idx_show_date", columnList = "showDate"),
        @Index(name = "idx_show_theatre", columnList = "theatreId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long movieId;

    private Long eventId;

    @Column(nullable = false)
    private Long theatreId;

    @Column(nullable = false)
    private Long screenId;

    @Column(nullable = false, length = 200)
    private String movieTitle;

    @Column(nullable = false, length = 150)
    private String theatreName;

    @Column(nullable = false, length = 100)
    private String screenName;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private LocalDate showDate;

    @Column(nullable = false)
    private LocalTime startTime;

    private LocalTime endTime;

    @Column(length = 100)
    private String soundExperience; // Dolby Atmos, IMAX 12-Track Immersive Audio

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private ScreenFormat screenFormat = ScreenFormat.REGULAR_2D;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ShowStatus status = ShowStatus.OPEN;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<ShowPriceTier> priceTiers = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
