package com.ticketflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {
    private boolean locked;
    private String lockId;
    private Long showId;
    private List<Long> lockedSeatIds;
    private List<Long> failedSeatIds;
    private Long expiresAtEpochMs;
    private String message;
}

