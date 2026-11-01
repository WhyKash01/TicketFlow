package com.ticketflow.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private String eventId;
    private String bookingId;
    private String userId;
    private String userEmail;
    private Long showId;
    private List<Long> seatIds;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

