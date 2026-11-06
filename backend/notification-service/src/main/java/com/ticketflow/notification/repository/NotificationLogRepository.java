package com.ticketflow.notification.repository;

import com.ticketflow.notification.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByBookingReferenceOrderBySentAtDesc(String bookingReference);
    List<NotificationLog> findByBookingIdOrderBySentAtDesc(String bookingId);
    List<NotificationLog> findByUserIdOrderBySentAtDesc(String userId);
    List<NotificationLog> findByRecipientOrderBySentAtDesc(String recipient);
    List<NotificationLog> findByTemplateCode(String templateCode);
}
