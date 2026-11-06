package com.ticketflow.notification.service;

import com.ticketflow.common.enums.NotificationChannel;
import com.ticketflow.common.events.BookingCancelledEvent;
import com.ticketflow.common.events.BookingConfirmedEvent;
import com.ticketflow.common.events.PaymentRefundedEvent;
import com.ticketflow.notification.model.NotificationLog;
import com.ticketflow.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketNotificationService {

    private final NotificationLogRepository logRepository;

    /**
     * Dispatches multi-channel booking confirmation (Email + SMS).
     */
    @Transactional
    public void sendBookingConfirmation(BookingConfirmedEvent event) {
        log.info("Preparing digital ticket e-mail & SMS for booking ref: {}", event.getBookingReference());

        String seats = (event.getSeatNumbers() != null) ? String.join(", ", event.getSeatNumbers()) : "N/A";
        String emailContent = String.format("""
                =================================================
                🎬 TICKETFLOW DIGITAL MOVIE PASS 🎟️
                =================================================
                Booking Ref: %s
                Movie: %s
                Cinema: %s - %s
                Show Time: %s
                Seats: %s
                Total Amount Paid: ₹%.2f
                Digital Token: %s
                =================================================
                Show this pass or QR token at the cinema entrance. Enjoy the movie!
                """,
                event.getBookingReference(),
                event.getMovieTitle(),
                event.getTheatreName(),
                event.getScreenName(),
                event.getShowTime(),
                seats,
                event.getTotalAmount(),
                event.getQrCodePayload());

        String recipientEmail = (event.getUserEmail() != null && !event.getUserEmail().isBlank())
                ? event.getUserEmail()
                : "guest@ticketflow.com";

        // 1. Email notification
        NotificationLog emailLog = NotificationLog.builder()
                .recipient(recipientEmail)
                .channel(NotificationChannel.EMAIL)
                .subject("Ticket Confirmed: " + event.getMovieTitle() + " [" + event.getBookingReference() + "]")
                .messageBody(emailContent)
                .templateCode("TICKET_CONFIRMATION")
                .bookingReference(event.getBookingReference())
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .status("SENT")
                .deliveryProvider("MOCK_SES")
                .sentAt(LocalDateTime.now())
                .build();
        logRepository.save(emailLog);

        // 2. SMS notification
        if (event.getUserPhone() != null && !event.getUserPhone().isBlank()) {
            NotificationLog smsLog = NotificationLog.builder()
                    .recipient(event.getUserPhone())
                    .channel(NotificationChannel.SMS)
                    .subject("TicketFlow SMS Alert")
                    .messageBody(String.format("TicketFlow: Tickets confirmed for %s (%s). Ref: %s. Enjoy the show!",
                            event.getMovieTitle(), seats, event.getBookingReference()))
                    .templateCode("TICKET_CONFIRMATION_SMS")
                    .bookingReference(event.getBookingReference())
                    .bookingId(event.getBookingId())
                    .userId(event.getUserId())
                    .status("SENT")
                    .deliveryProvider("MOCK_TWILIO")
                    .sentAt(LocalDateTime.now())
                    .build();
            logRepository.save(smsLog);
        }

        log.info("Ticket confirmation delivered for bookingRef: {}", event.getBookingReference());
    }

    /**
     * Dispatches cancellation notice when a booking is cancelled or rolled back.
     */
    @Transactional
    public void sendBookingCancellation(BookingCancelledEvent event) {
        log.info("Preparing cancellation notification for bookingId: {}, reason: {}", event.getBookingId(), event.getReason());

        String ref = (event.getBookingReference() != null) ? event.getBookingReference() : event.getBookingId();
        String recipient = (event.getUserEmail() != null && !event.getUserEmail().isBlank())
                ? event.getUserEmail()
                : "guest@ticketflow.com";

        String messageBody = String.format("""
                =================================================
                ❌ TICKETFLOW BOOKING CANCELLED
                =================================================
                Booking Reference: %s
                Reason: %s
                Seats have been unlocked and released back to inventory.
                Any deducted funds will be automatically credited to your source payment method within 3-5 business days.
                =================================================
                """,
                ref,
                event.getReason() != null ? event.getReason() : "Customer requested or reservation expired");

        NotificationLog cancellationEmail = NotificationLog.builder()
                .recipient(recipient)
                .channel(NotificationChannel.EMAIL)
                .subject("Booking Cancelled: [" + ref + "]")
                .messageBody(messageBody)
                .templateCode("BOOKING_CANCELLATION")
                .bookingReference(ref)
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .status("SENT")
                .deliveryProvider("MOCK_SES")
                .sentAt(LocalDateTime.now())
                .build();
        logRepository.save(cancellationEmail);

        if (event.getUserPhone() != null && !event.getUserPhone().isBlank()) {
            NotificationLog smsLog = NotificationLog.builder()
                    .recipient(event.getUserPhone())
                    .channel(NotificationChannel.SMS)
                    .subject("TicketFlow Cancellation Alert")
                    .messageBody("TicketFlow: Booking " + ref + " has been cancelled. Any deducted amount is refunded.")
                    .templateCode("BOOKING_CANCELLATION_SMS")
                    .bookingReference(ref)
                    .bookingId(event.getBookingId())
                    .userId(event.getUserId())
                    .status("SENT")
                    .deliveryProvider("MOCK_TWILIO")
                    .sentAt(LocalDateTime.now())
                    .build();
            logRepository.save(smsLog);
        }

        log.info("Cancellation notice delivered for bookingId: {}", event.getBookingId());
    }

    /**
     * Dispatches refund confirmation notice when payment refund occurs.
     */
    @Transactional
    public void sendPaymentRefundNotification(PaymentRefundedEvent event) {
        log.info("Preparing refund notification for refundId: {}, amount: {}", event.getRefundId(), event.getRefundedAmount());

        String recipient = (event.getUserId() != null) ? event.getUserId() : "user@ticketflow.com";
        String messageBody = String.format("""
                =================================================
                💰 TICKETFLOW REFUND CONFIRMATION
                =================================================
                Refund Reference: %s
                Original Transaction: %s
                Booking ID: %s
                Refunded Amount: ₹%.2f
                Reason: %s
                Timeline: Your bank will reflect these funds in 3-5 business days.
                =================================================
                """,
                event.getRefundId(),
                event.getTransactionId(),
                event.getBookingId(),
                event.getRefundedAmount(),
                event.getReason() != null ? event.getReason() : "Saga Compensation / Cancellation");

        NotificationLog refundEmail = NotificationLog.builder()
                .recipient(recipient)
                .channel(NotificationChannel.EMAIL)
                .subject("Refund Processed: ₹" + event.getRefundedAmount() + " [Ref: " + event.getRefundId() + "]")
                .messageBody(messageBody)
                .templateCode("PAYMENT_REFUND")
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .status("SENT")
                .deliveryProvider("MOCK_SES")
                .sentAt(LocalDateTime.now())
                .build();
        logRepository.save(refundEmail);

        log.info("Refund notification recorded for refundId: {}", event.getRefundId());
    }

    public List<NotificationLog> getNotificationsForBooking(String bookingReference) {
        return logRepository.findByBookingReferenceOrderBySentAtDesc(bookingReference);
    }

    public List<NotificationLog> getNotificationsForUser(String userId) {
        return logRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public List<NotificationLog> getNotificationsForRecipient(String recipient) {
        return logRepository.findByRecipientOrderBySentAtDesc(recipient);
    }
}
