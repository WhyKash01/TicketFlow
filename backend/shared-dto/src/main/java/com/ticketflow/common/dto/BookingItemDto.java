package com.ticketflow.common.dto;

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
public class BookingItemDto {
    private Long id;
    private Long showSeatId;
    private String seatNumber;
    private String rowName;
    private Integer colIndex;
    private SeatTier tier;
    private BigDecimal price;
}

