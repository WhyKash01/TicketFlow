package com.ticketflow.common.dto;

import com.ticketflow.common.enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String bookingId;
    private String userId;
    private BigDecimal amount;
    private String currency; // INR, USD
    private String paymentMethod; // UPI, CREDIT_CARD, NET_BANKING
    private String idempotencyKey;
    private boolean simulateFailure; // For testing Saga compensation & rollback
    @Builder.Default
    private PaymentProvider provider = PaymentProvider.MOCK;
}
