package com.ticketflow.common.events;

import com.ticketflow.common.enums.PaymentStatus;
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
public class PaymentProcessedEvent {
    private String transactionId;
    private String bookingId;
    private String userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String failureReason;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

