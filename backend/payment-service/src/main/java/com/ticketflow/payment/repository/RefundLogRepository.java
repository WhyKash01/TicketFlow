package com.ticketflow.payment.repository;

import com.ticketflow.payment.model.RefundLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundLogRepository extends JpaRepository<RefundLog, String> {
    List<RefundLog> findByTransactionIdOrderByCreatedAtDesc(String transactionId);
    List<RefundLog> findByBookingIdOrderByCreatedAtDesc(String bookingId);
    Optional<RefundLog> findByIdempotencyKey(String idempotencyKey);
}

