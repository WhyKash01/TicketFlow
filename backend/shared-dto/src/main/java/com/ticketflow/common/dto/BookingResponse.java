package com.ticketflow.common.dto;

import com.ticketflow.common.enums.BookingStatus;
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
public class BookingResponse {
    private String bookingId;
    private String bookingReference;
    private String userId;
    private Long showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String showTime;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String qrCodePayload;
    private LocalDateTime createdAt;
    private String message;
}

