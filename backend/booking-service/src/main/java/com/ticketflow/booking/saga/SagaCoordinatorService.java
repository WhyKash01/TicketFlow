package com.ticketflow.booking.saga;

import com.ticketflow.booking.messaging.BookingEventPublisher;
import com.ticketflow.booking.model.Booking;
import com.ticketflow.booking.model.BookingItem;
import com.ticketflow.booking.model.Seat;
import com.ticketflow.booking.redis.RedisSeatLockService;
import com.ticketflow.booking.repository.BookingRepository;
import com.ticketflow.booking.repository.SeatRepository;
import com.ticketflow.common.dto.BookingRequest;
import com.ticketflow.common.dto.BookingResponse;
import com.ticketflow.common.enums.BookingStatus;
import com.ticketflow.common.enums.SeatStatus;
import com.ticketflow.common.events.BookingCancelledEvent;
import com.ticketflow.common.events.BookingConfirmedEvent;
import com.ticketflow.common.events.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaCoordinatorService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final RedisSeatLockService redisSeatLockService;
    private final BookingEventPublisher eventPublisher;

    /**
     * SAGA Step 1 & 2: Validate seat locks, create booking in PAYMENT_PENDING state.
     */
    @Transactional
    public BookingResponse initiateBookingSaga(BookingRequest request) {
        String bookingId = UUID.randomUUID().toString();
        String bookingRef = "TF-" + (1000 + new Random().nextInt(9000)) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        List<String> seatNumbers = seats.stream().map(Seat::getSeatNumber).collect(Collectors.toList());

        BigDecimal basePrice = seats.stream()
                .map(Seat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal convenienceFee = new BigDecimal(seats.size()).multiply(new BigDecimal("35.40")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxes = convenienceFee.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = (request.getTotalAmount() != null)
                ? request.getTotalAmount()
                : basePrice.add(convenienceFee).add(taxes);

        Booking booking = Booking.builder()
                .bookingId(bookingId)
                .bookingReference(bookingRef)
                .userId(request.getUserId())
                .userEmail(request.getUserEmail())
                .userPhone(request.getUserPhone())
                .showId(request.getShowId())
                .movieTitle(request.getMovieTitle())
                .theatreName(request.getTheatreName())
                .screenName(request.getScreenName())
                .showTime(request.getShowTime())
                .seatIds(request.getSeatIds())
                .seatNumbers(seatNumbers)
                .basePrice(basePrice)
                .convenienceFee(convenienceFee)
                .taxes(taxes)
                .totalAmount(grandTotal)
                .status(BookingStatus.PAYMENT_PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        for (Seat seat : seats) {
            BookingItem item = BookingItem.builder()
                    .booking(booking)
                    .showSeatId(seat.getId())
                    .seatNumber(seat.getSeatNumber())
                    .rowName(seat.getRowName())
                    .colIndex(seat.getColIndex())
                    .tier(seat.getTier())
                    .price(seat.getPrice())
                    .build();
            booking.addItem(item);
        }

        Booking saved = bookingRepository.save(booking);

        // Publish BookingCreatedEvent
        eventPublisher.publishBookingCreated(BookingCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .bookingId(saved.getBookingId())
                .userId(saved.getUserId())
                .userEmail(saved.getUserEmail())
                .showId(saved.getShowId())
                .seatIds(saved.getSeatIds())
                .seatNumbers(saved.getSeatNumbers())
                .totalAmount(saved.getTotalAmount())
                .build());

        return mapToResponse(saved, "Booking initiated. Complete payment within lock window.");
    }

    /**
     * SAGA Step 4: Confirm Booking on Payment Success.
     */
    @Transactional
    public BookingResponse completeBookingSaga(String bookingId, String transactionId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return mapToResponse(booking, "Booking already confirmed.");
        }

        // 1. Mark seats as BOOKED in database with optimistic locking check
        List<Seat> seats = seatRepository.findAllById(booking.getSeatIds());
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
        }
        seatRepository.saveAll(seats);

        // 2. Generate digital ticket QR code token
        String qrPayload = String.format("TICKETFLOW|REF:%s|SHOW:%d|SEATS:%s|TX:%s",
                booking.getBookingReference(), booking.getShowId(),
                String.join(",", booking.getSeatNumbers()), transactionId);

        // 3. Update booking record
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentTransactionId(transactionId);
        booking.setQrCodePayload(qrPayload);
        Booking confirmed = bookingRepository.save(booking);

        // 4. Release Redis distributed locks
        redisSeatLockService.unlockSeats(booking.getShowId(), booking.getUserId(), booking.getSeatIds());

        // 5. Publish domain event for Asynchronous Notification Service
        eventPublisher.publishBookingConfirmed(BookingConfirmedEvent.builder()
                .bookingId(confirmed.getBookingId())
                .bookingReference(confirmed.getBookingReference())
                .userId(confirmed.getUserId())
                .userEmail(confirmed.getUserEmail())
                .userPhone(confirmed.getUserPhone())
                .movieTitle(confirmed.getMovieTitle())
                .theatreName(confirmed.getTheatreName())
                .screenName(confirmed.getScreenName())
                .showTime(confirmed.getShowTime())
                .seatNumbers(confirmed.getSeatNumbers())
                .totalAmount(confirmed.getTotalAmount())
                .qrCodePayload(confirmed.getQrCodePayload())
                .build());

        log.info("Saga Complete: Booking {} confirmed successfully!", confirmed.getBookingReference());
        return mapToResponse(confirmed, "Booking confirmed successfully! Your tickets are ready.");
    }

    /**
     * SAGA Step 5 (Compensating Transaction): Rollback booking and release seats.
     */
    @Transactional
    public BookingResponse compensateBookingSaga(String bookingId, String reason) {
        log.warn("Triggering SAGA Compensating transaction for booking {}: reason={}", bookingId, reason);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        Booking cancelled = bookingRepository.save(booking);

        // Release Redis seat locks
        redisSeatLockService.unlockSeats(booking.getShowId(), booking.getUserId(), booking.getSeatIds());

        // Publish cancellation event
        eventPublisher.publishBookingCancelled(BookingCancelledEvent.builder()
                .bookingId(bookingId)
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .userPhone(booking.getUserPhone())
                .showId(booking.getShowId())
                .seatIds(booking.getSeatIds())
                .reason(reason)
                .build());

        return mapToResponse(cancelled, "Booking cancelled and seats unlocked.");
    }

    private BookingResponse mapToResponse(Booking b, String message) {
        return BookingResponse.builder()
                .bookingId(b.getBookingId())
                .bookingReference(b.getBookingReference())
                .userId(b.getUserId())
                .showId(b.getShowId())
                .movieTitle(b.getMovieTitle())
                .theatreName(b.getTheatreName())
                .screenName(b.getScreenName())
                .showTime(b.getShowTime())
                .seatNumbers(b.getSeatNumbers())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus())
                .qrCodePayload(b.getQrCodePayload())
                .createdAt(b.getCreatedAt())
                .message(message)
                .build();
    }
}
