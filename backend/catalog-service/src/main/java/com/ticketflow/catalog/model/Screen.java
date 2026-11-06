package com.ticketflow.catalog.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ticketflow.common.enums.ScreenFormat;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screens", indexes = {
        @Index(name = "idx_screen_theatre", columnList = "theatre_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // e.g. "Audi 1", "Screen 1 (IMAX Laser)"

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private ScreenFormat screenFormat = ScreenFormat.REGULAR_2D;

    private Integer totalRows;   // e.g. 8 (A to H)
    private Integer seatsPerRow; // e.g. 12
    private Integer totalCapacity; // e.g. 96

    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    @JsonBackReference
    private Theatre theatre;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<ScreenSeat> screenSeats = new ArrayList<>();
}
