package com.ticketflow.notification.service;

import com.ticketflow.common.events.BookingConfirmedEvent;
import com.ticketflow.notification.model.NotificationLog;
import com.ticketflow.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketNotificationService {

    private final NotificationLogRepository logRepository;

    public void sendBookingConfirmation(BookingConfirmedEvent event) {
        log.info("Preparing digital ticket e-mail & SMS for booking ref: {}", event.getBookingReference());

        String emailContent = String.format("""
                🎬 TICKETFLOW BOOKING CONFIRMED! 🎟️
                =================================================
                Booking ID: %s
                Movie: %s
                Cinema: %s - %s
                Show Time: %s
                Seats: %s
                Total Paid: ₹%.2f
                Digital Token: %s
                =================================================
                Show this ticket QR at the cinema entrance. Enjoy your movie!
                """,
                event.getBookingReference(),
                event.getMovieTitle(),
                event.getTheatreName(),
                event.getScreenName(),
                event.getShowTime(),
                String.join(", ", event.getSeatNumbers()),
                event.getTotalAmount(),
                event.getQrCodePayload());

        // 1. Log Email dispatch
        NotificationLog emailLog = NotificationLog.builder()
                .recipient(event.getUserEmail() != null ? event.getUserEmail() : "guest@ticketflow.com")
                .channel("EMAIL")
                .subject("Ticket Confirmed: " + event.getMovieTitle() + " [" + event.getBookingReference() + "]")
                .messageBody(emailContent)
                .bookingReference(event.getBookingReference())
                .status("SENT")
                .sentAt(LocalDateTime.now())
                .build();
        logRepository.save(emailLog);

        // 2. Log SMS dispatch
        if (event.getUserPhone() != null && !event.getUserPhone().isBlank()) {
            NotificationLog smsLog = NotificationLog.builder()
                    .recipient(event.getUserPhone())
                    .channel("SMS")
                    .subject("TicketFlow Alert")
                    .messageBody("Ticket confirmed for " + event.getMovieTitle() + " (" + String.join(",", event.getSeatNumbers()) + "). Ref: " + event.getBookingReference())
                    .bookingReference(event.getBookingReference())
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .build();
            logRepository.save(smsLog);
        }

        log.info("Ticket notification delivered for {}", event.getBookingReference());
    }

    public List<NotificationLog> getNotificationsForBooking(String bookingReference) {
        return logRepository.findByBookingReference(bookingReference);
    }
}

