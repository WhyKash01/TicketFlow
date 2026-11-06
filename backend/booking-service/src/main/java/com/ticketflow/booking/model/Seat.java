package com.ticketflow.booking.model;

import com.ticketflow.common.enums.SeatStatus;
import com.ticketflow.common.enums.SeatTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seats", indexes = {
        @Index(name = "idx_show_seats", columnList = "showId, rowName"),
        @Index(name = "idx_show_seat_number", columnList = "showId, seatNumber")
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

    @Column(nullable = false, length = 10)
    private String seatNumber; // "A1", "A2", "D5"

    @Column(nullable = false, length = 5)
    private String rowName; // "A", "B", "C"

    @Column(nullable = false)
    private Integer colIndex; // 1, 2, 3...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatTier tier; // PLATINUM, GOLD, SILVER

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status; // AVAILABLE, LOCKED, BOOKED

    @Version
    @Builder.Default
    private Long version = 0L;
}
