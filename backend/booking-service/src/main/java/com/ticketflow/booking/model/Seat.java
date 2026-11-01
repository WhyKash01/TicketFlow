package com.ticketflow.booking.model;

import com.ticketflow.common.enums.SeatStatus;
import com.ticketflow.common.enums.SeatTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seats", indexes = {
        @Index(name = "idx_show_seats", columnList = "showId, rowName")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long showId;

    @Column(nullable = false)
    private String seatNumber; // "A1", "A2", "D5"

    @Column(nullable = false)
    private String rowName; // "A", "B", "C"

    @Column(nullable = false)
    private Integer colIndex; // 1, 2, 3...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatTier tier; // PLATINUM, GOLD, SILVER

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status; // AVAILABLE, LOCKED, BOOKED
}

