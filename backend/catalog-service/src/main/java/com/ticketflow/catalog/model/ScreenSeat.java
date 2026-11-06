package com.ticketflow.catalog.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ticketflow.common.enums.SeatTier;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "screen_seats", indexes = {
        @Index(name = "idx_screen_seat", columnList = "screen_id, rowName, colIndex")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @JsonBackReference
    private Screen screen;

    @Column(nullable = false, length = 10)
    private String seatNumber; // "A1", "D5"

    @Column(nullable = false, length = 5)
    private String rowName; // "A", "B", "C"

    @Column(nullable = false)
    private Integer colIndex; // 1, 2, 3...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SeatTier defaultTier = SeatTier.GOLD;

    @Builder.Default
    private Boolean isAccessible = false; // Wheelchair friendly
}

