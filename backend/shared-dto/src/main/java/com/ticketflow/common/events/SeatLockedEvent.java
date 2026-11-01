package com.ticketflow.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockedEvent {
    private String lockId;
    private Long showId;
    private String userId;
    private List<Long> seatIds;
    private Long expiresAtEpochMs;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

