package com.ticketflow.booking.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ticketflow.common.enums.SeatTier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_items", indexes = {
        @Index(name = "idx_booking_items_booking", columnList = "booking_id"),
        @Index(name = "idx_booking_items_seat", columnList = "show_seat_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonBackReference
    private Booking booking;

    @Column(name = "show_seat_id", nullable = false)
    private Long showSeatId;

    @Column(nullable = false, length = 10)
    private String seatNumber; // "A1", "D5"

    @Column(nullable = false, length = 5)
    private String rowName; // "A", "B"

    @Column(nullable = false)
    private Integer colIndex; // 1, 2, 3...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatTier tier;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;
}

