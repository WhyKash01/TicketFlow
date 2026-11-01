package com.ticketflow.booking.controller;

import com.ticketflow.booking.model.Booking;
import com.ticketflow.booking.redis.RedisSeatLockService;
import com.ticketflow.booking.saga.SagaCoordinatorService;
import com.ticketflow.booking.service.BookingService;
import com.ticketflow.booking.service.SegmentTreeAllocationService;
import com.ticketflow.common.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final RedisSeatLockService redisSeatLockService;
    private final SegmentTreeAllocationService segmentTreeAllocationService;
    private final SagaCoordinatorService sagaCoordinatorService;

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<ApiResponse<List<SeatDto>>> getShowSeats(
            @PathVariable Long showId,
            @RequestParam(required = false) String userId) {
        List<SeatDto> seats = bookingService.getShowSeatsWithRealtimeStatus(showId, userId);
        return ResponseEntity.ok(ApiResponse.success(seats, "Seats retrieved successfully"));
    }

    @PostMapping("/lock-seats")
    public ResponseEntity<ApiResponse<SeatLockResponse>> lockSeats(@RequestBody SeatLockRequest request) {
        SeatLockResponse response = redisSeatLockService.lockSeats(request);
        if (response.isLocked()) {
            return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
        } else {
            return ResponseEntity.status(409).body(ApiResponse.failure(response.getMessage()));
        }
    }

    @PostMapping("/unlock-seats")
    public ResponseEntity<ApiResponse<Boolean>> unlockSeats(
            @RequestParam Long showId,
            @RequestParam String userId,
            @RequestBody List<Long> seatIds) {
        boolean released = redisSeatLockService.unlockSeats(showId, userId, seatIds);
        return ResponseEntity.ok(ApiResponse.success(released, "Seats unlocked successfully"));
    }

    @PostMapping("/auto-allocate")
    public ResponseEntity<ApiResponse<ContiguousSeatResponse>> autoAllocateContiguousSeats(
            @RequestBody ContiguousSeatRequest request) {
        ContiguousSeatResponse response = segmentTreeAllocationService.allocateContiguousSeats(request);
        if (response.isFound()) {
            return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.failure(response.getMessage()));
        }
    }

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<BookingResponse>> initiateBooking(@RequestBody BookingRequest request) {
        BookingResponse response = sagaCoordinatorService.initiateBookingSaga(request);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "TX-MOCK-SUCCESS") String transactionId) {
        BookingResponse response = sagaCoordinatorService.completeBookingSaga(bookingId, transactionId);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String bookingId,
            @RequestParam(defaultValue = "User cancelled or payment failed") String reason) {
        BookingResponse response = sagaCoordinatorService.compensateBookingSaga(bookingId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(@PathVariable String bookingId) {
        return bookingService.getBookingById(bookingId)
                .map(b -> ResponseEntity.ok(ApiResponse.success(b, "Booking found")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Booking not found: " + bookingId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Booking>>> getUserBookings(@PathVariable String userId) {
        List<Booking> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(ApiResponse.success(bookings, "User bookings retrieved"));
    }
}

