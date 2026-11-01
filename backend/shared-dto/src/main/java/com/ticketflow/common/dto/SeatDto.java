package com.ticketflow.common.dto;

import com.ticketflow.common.enums.SeatStatus;
import com.ticketflow.common.enums.SeatTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
    private Long id;
    private Long showId;
    private String seatNumber; // e.g., "A1", "B5"
    private String rowName;    // "A", "B", "C"
    private Integer colIndex;  // 1, 2, 3...
    private SeatTier tier;     // PLATINUM, GOLD, SILVER
    private BigDecimal price;  // 350.00, 220.00, etc.
    private SeatStatus status; // AVAILABLE, LOCKED, BOOKED
    private String lockedByUserId;
    private Long lockExpiresAtEpochMs;
}

