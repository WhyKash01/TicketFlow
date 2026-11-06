package com.ticketflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private String refundId;
    private String transactionId;
    private String bookingId;
    private BigDecimal refundAmount;
    private String refundReference;
    private String status; // REFUND_SUCCESS, REFUND_FAILED
    private String reason;
    private LocalDateTime timestamp;
}

