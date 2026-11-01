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
public class BookingCancelledEvent {
    private String bookingId;
    private Long showId;
    private List<Long> seatIds;
    private String reason;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

