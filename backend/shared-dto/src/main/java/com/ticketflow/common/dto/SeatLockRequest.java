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
public class SeatLockRequest {
    private Long showId;
    private String userId;
    private List<Long> seatIds;
    @Builder.Default
    private Long ttlSeconds = 600L; // 10 minutes default Redis lock TTL
}

