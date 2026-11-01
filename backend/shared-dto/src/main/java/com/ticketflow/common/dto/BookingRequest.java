package com.ticketflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String userId;
    private String userEmail;
    private String userPhone;
    private Long showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private String showTime;
    private List<Long> seatIds;
    private String lockId;
    private BigDecimal totalAmount;
    private String paymentMethod; // UPI, CARD, NETBANKING
}

