package com.ticketflow.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_logs", indexes = {
        @Index(name = "idx_refund_tx_id", columnList = "transactionId"),
        @Index(name = "idx_refund_booking_id", columnList = "bookingId"),
        @Index(name = "idx_refund_idempotency", columnList = "idempotencyKey", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundLog {

    @Id
    @Column(length = 36)
    private String refundId;

    @Column(nullable = false, length = 36)
    private String transactionId;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private BigDecimal refundAmount;

    private String refundReference;

    private String reason;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String status; // REFUND_SUCCESS, REFUND_FAILED

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}

