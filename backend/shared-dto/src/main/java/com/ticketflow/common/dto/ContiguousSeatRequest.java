package com.ticketflow.common.dto;

import com.ticketflow.common.enums.SeatTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContiguousSeatRequest {
    private Long showId;
    private Integer seatCount; // e.g., 3 seats requested together
    private SeatTier preferredTier; // Optional tier preference (e.g., GOLD)
    private String preferredRow;    // Optional specific row preference
}

