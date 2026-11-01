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
        return ResponseEntity.ok(ApiResponse.success(logs, "Notification history fetched"));
    }
}

