package com.ticketflow.notification.controller;

import com.ticketflow.common.dto.ApiResponse;
import com.ticketflow.notification.model.NotificationLog;
import com.ticketflow.notification.service.TicketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final TicketNotificationService notificationService;

    @GetMapping("/booking/{bookingReference}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getBookingNotifications(
            @PathVariable String bookingReference) {
        List<NotificationLog> logs = notificationService.getNotificationsForBooking(bookingReference);
        return ResponseEntity.ok(ApiResponse.success(logs, "Notification history fetched for booking reference"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getUserNotifications(
            @PathVariable String userId) {
        List<NotificationLog> logs = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(logs, "Notification history fetched for user"));
    }

    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> getRecipientNotifications(
            @PathVariable String recipient) {
        List<NotificationLog> logs = notificationService.getNotificationsForRecipient(recipient);
        return ResponseEntity.ok(ApiResponse.success(logs, "Notification history fetched for recipient"));
    }
}
