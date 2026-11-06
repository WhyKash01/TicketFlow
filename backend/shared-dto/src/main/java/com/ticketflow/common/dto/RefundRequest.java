package com.ticketflow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private String bookingId;
    private String transactionId;
    private BigDecimal amount;
    private String reason;
    private String idempotencyKey;
}

