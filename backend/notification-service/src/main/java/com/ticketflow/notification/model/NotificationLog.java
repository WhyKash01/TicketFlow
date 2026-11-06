package com.ticketflow.notification.model;

import com.ticketflow.common.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notif_booking_ref", columnList = "bookingReference"),
        @Index(name = "idx_notif_user_id", columnList = "userId"),
        @Index(name = "idx_notif_recipient", columnList = "recipient")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationChannel channel;

    private String subject;

    @Column(length = 4000)
    private String messageBody;

    private String templateCode;

    private String bookingReference;

    private String bookingId;

    private String userId;

    @Builder.Default
    private String status = "SENT";

    @Builder.Default
    private String deliveryProvider = "MOCK_GATEWAY";

    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) sentAt = LocalDateTime.now();
        if (status == null) status = "SENT";
        if (deliveryProvider == null) deliveryProvider = "MOCK_GATEWAY";
    }
}
