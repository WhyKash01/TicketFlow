package com.ticketflow.common.dto;

import com.ticketflow.common.enums.PaymentProvider;
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
public class PaymentResponse {
    private String transactionId;
    private String bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentProvider provider;
    private String gatewayReference;
    private String failureReason;
    private String refundReference;
    private BigDecimal refundedAmount;
    private LocalDateTime timestamp;
}
