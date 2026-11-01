package com.ticketflow.booking.service;

import com.ticketflow.booking.model.Booking;
import com.ticketflow.booking.model.Seat;
import com.ticketflow.booking.redis.RedisSeatLockService;
import com.ticketflow.booking.repository.BookingRepository;
import com.ticketflow.booking.repository.SeatRepository;
import com.ticketflow.common.dto.SeatDto;
import com.ticketflow.common.enums.SeatStatus;
import com.ticketflow.common.enums.SeatTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final RedisSeatLockService redisSeatLockService;

    /**
     * Retrieves all seats for a show with real-time status (merging DB state with Redis live locks).
     */
    public List<SeatDto> getShowSeatsWithRealtimeStatus(Long showId, String currentUserId) {
        List<Seat> seats = seatRepository.findByShowIdOrderByRowNameAscColIndexAsc(showId);
        if (seats.isEmpty()) {
            seats = initializeDefaultShowSeats(showId);
        }

        return seats.stream().map(seat -> {
            SeatStatus effectiveStatus = seat.getStatus();
            String lockedBy = null;

            if (effectiveStatus == SeatStatus.AVAILABLE) {
                if (redisSeatLockService.isSeatLocked(showId, seat.getId())) {
                    effectiveStatus = SeatStatus.LOCKED;
                    lockedBy = redisSeatLockService.getSeatLockOwner(showId, seat.getId());
                }
            }

            return SeatDto.builder()
                    .id(seat.getId())
                    .showId(seat.getShowId())
                    .seatNumber(seat.getSeatNumber())
                    .rowName(seat.getRowName())
                    .colIndex(seat.getColIndex())
                    .tier(seat.getTier())
                    .price(seat.getPrice())
                    .status(effectiveStatus)
                    .lockedByUserId(lockedBy)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<Booking> getUserBookings(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Booking> getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public Optional<Booking> getBookingByReference(String bookingRef) {
        return bookingRepository.findByBookingReference(bookingRef);
    }

    /**
     * Seeds realistic cinema hall seat layout (Rows A to H, 12 seats each, tiered pricing).
     */
    public List<Seat> initializeDefaultShowSeats(Long showId) {
        log.info("Initializing seat layout for show: {}", showId);
        List<Seat> list = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};

        for (String r : rows) {
            SeatTier tier;
            BigDecimal price;

            if (r.equals("A") || r.equals("B")) {
                tier = SeatTier.PLATINUM;
                price = new BigDecimal("450.00");
            } else if (r.equals("C") || r.equals("D") || r.equals("E")) {
                tier = SeatTier.GOLD;
                price = new BigDecimal("320.00");
            } else {
                tier = SeatTier.SILVER;
                price = new BigDecimal("220.00");
            }

            for (int c = 1; c <= 12; c++) {
                list.add(Seat.builder()
                        .showId(showId)
                        .rowName(r)
                        .colIndex(c)
                        .seatNumber(r + c)
                        .tier(tier)
                        .price(price)
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        }

        return seatRepository.saveAll(list);
    }
}

