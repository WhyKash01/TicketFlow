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
public class BookingConfirmedEvent {
    private String bookingId;
    private String bookingReference;
    private String userId;
    private String userEmail;
    private String userPhone;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String showTime;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String qrCodePayload;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

