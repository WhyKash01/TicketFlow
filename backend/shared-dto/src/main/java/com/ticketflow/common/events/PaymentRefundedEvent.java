package com.ticketflow.common.events;

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
public class PaymentRefundedEvent {
    private String refundId;
    private String transactionId;
    private String bookingId;
    private String userId;
    private BigDecimal refundedAmount;
    private String reason;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

