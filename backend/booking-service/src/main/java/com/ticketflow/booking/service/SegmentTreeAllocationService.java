package com.ticketflow.booking.service;

import com.ticketflow.booking.algorithm.SegmentTree;
import com.ticketflow.booking.model.Seat;
import com.ticketflow.booking.redis.RedisSeatLockService;
import com.ticketflow.booking.repository.SeatRepository;
import com.ticketflow.common.dto.ContiguousSeatRequest;
import com.ticketflow.common.dto.ContiguousSeatResponse;
import com.ticketflow.common.dto.SeatDto;
import com.ticketflow.common.enums.SeatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SegmentTreeAllocationService {

    private final SeatRepository seatRepository;
    private final RedisSeatLockService redisSeatLockService;

    public ContiguousSeatResponse allocateContiguousSeats(ContiguousSeatRequest request) {
        Long showId = request.getShowId();
        int requestedCount = request.getSeatCount();

        if (requestedCount <= 0 || requestedCount > 10) {
            return ContiguousSeatResponse.builder()
                    .found(false)
                    .showId(showId)
                    .message("Requested seat count must be between 1 and 10.")
                    .build();
        }

        List<Seat> allSeats = seatRepository.findByShowIdOrderByRowNameAscColIndexAsc(showId);
        if (allSeats.isEmpty()) {
            return ContiguousSeatResponse.builder()
                    .found(false)
                    .showId(showId)
                    .message("No seats configured for showId: " + showId)
                    .build();
        }

        // Group seats by row
        Map<String, List<Seat>> seatsByRow = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRowName, LinkedHashMap::new, Collectors.toList()));

        // Filter rows by preferredTier or preferredRow if specified
        List<String> candidateRows = new ArrayList<>(seatsByRow.keySet());
        if (request.getPreferredRow() != null && !request.getPreferredRow().isBlank()) {
            candidateRows = Collections.singletonList(request.getPreferredRow().toUpperCase());
        }

        for (String rowName : candidateRows) {
            List<Seat> rowSeats = seatsByRow.get(rowName);
            if (rowSeats == null || rowSeats.size() < requestedCount) continue;

            // Check preferred tier match if requested
            if (request.getPreferredTier() != null && rowSeats.get(0).getTier() != request.getPreferredTier()) {
                continue;
            }

            // Build binary availability array: 1 = Available, 0 = Booked or Locked
            int[] availability = new int[rowSeats.size()];
            for (int i = 0; i < rowSeats.size(); i++) {
                Seat seat = rowSeats.get(i);
                boolean isLocked = redisSeatLockService.isSeatLocked(showId, seat.getId());
                boolean isAvailable = (seat.getStatus() == SeatStatus.AVAILABLE) && !isLocked;
                availability[i] = isAvailable ? 1 : 0;
            }

            // Build Segment Tree for row
            SegmentTree segmentTree = new SegmentTree(availability);
            int startIndex = segmentTree.findContiguous(requestedCount);

            if (startIndex != -1) {
                // Found contiguous block of seats!
                List<SeatDto> allocated = new ArrayList<>();
                for (int i = startIndex; i < startIndex + requestedCount; i++) {
                    Seat seat = rowSeats.get(i);
                    allocated.add(SeatDto.builder()
                            .id(seat.getId())
                            .showId(seat.getShowId())
                            .seatNumber(seat.getSeatNumber())
                            .rowName(seat.getRowName())
                            .colIndex(seat.getColIndex())
                            .tier(seat.getTier())
                            .price(seat.getPrice())
                            .status(SeatStatus.AVAILABLE)
                            .build());
                }

                log.info("SegmentTree found {} contiguous seats in row {} starting at colIndex {}",
                        requestedCount, rowName, startIndex + 1);

                return ContiguousSeatResponse.builder()
                        .found(true)
                        .showId(showId)
                        .rowName(rowName)
                        .allocatedSeats(allocated)
                        .algorithmUsed("SegmentTree-ContiguousAllocation")
                        .message("Successfully found " + requestedCount + " contiguous seats in Row " + rowName)
                        .build();
            }
        }

        return ContiguousSeatResponse.builder()
                .found(false)
                .showId(showId)
                .algorithmUsed("SegmentTree-ContiguousAllocation")
                .message("No contiguous " + requestedCount + " seats available in any row for this show.")
                .build();
    }
}

