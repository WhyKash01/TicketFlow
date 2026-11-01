package com.ticketflow.booking.redis;

import com.ticketflow.common.dto.SeatLockRequest;
import com.ticketflow.common.dto.SeatLockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSeatLockService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_PREFIX = "lock:show:%d:seat:%d";

    private String buildKey(Long showId, Long seatId) {
        return String.format(LOCK_PREFIX, showId, seatId);
    }

    /**
     * Atomically acquires distributed locks for all requested seats.
     * If any seat is already locked, rollback all locks acquired in this batch.
     */
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        Long showId = request.getShowId();
        String userId = request.getUserId();
        List<Long> seatIds = request.getSeatIds();
        long ttlSeconds = (request.getTtlSeconds() != null) ? request.getTtlSeconds() : 600L;

        String lockId = UUID.randomUUID().toString();
        String lockValue = userId + "|" + lockId;
        List<Long> successfullyLocked = new ArrayList<>();
        List<Long> failedSeatIds = new ArrayList<>();

        for (Long seatId : seatIds) {
            String key = buildKey(showId, seatId);
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key, lockValue, Duration.ofSeconds(ttlSeconds));

            if (Boolean.TRUE.equals(acquired)) {
                successfullyLocked.add(seatId);
            } else {
                // Check if already locked by the SAME user
                String existing = redisTemplate.opsForValue().get(key);
                if (existing != null && existing.startsWith(userId + "|")) {
                    // Refresh TTL for current user
                    redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
                    successfullyLocked.add(seatId);
                } else {
                    failedSeatIds.add(seatId);
                    log.warn("Seat {} for show {} is already locked by another user", seatId, showId);
                    break; // Abort and rollback
                }
            }
        }

        // If any failed, rollback all acquired seats in this attempt
        if (!failedSeatIds.isEmpty()) {
            for (Long acquiredSeatId : successfullyLocked) {
                String key = buildKey(showId, acquiredSeatId);
                String current = redisTemplate.opsForValue().get(key);
                if (lockValue.equals(current)) {
                    redisTemplate.delete(key);
                }
            }
            return SeatLockResponse.builder()
                    .locked(false)
                    .showId(showId)
                    .lockedSeatIds(Collections.emptyList())
                    .failedSeatIds(failedSeatIds)
                    .message("One or more seats are currently locked by another user. Please choose different seats.")
                    .build();
        }

        long expiresAtEpochMs = Instant.now().plusSeconds(ttlSeconds).toEpochMilli();

        return SeatLockResponse.builder()
                .locked(true)
                .lockId(lockId)
                .showId(showId)
                .lockedSeatIds(successfullyLocked)
                .failedSeatIds(Collections.emptyList())
                .expiresAtEpochMs(expiresAtEpochMs)
                .message("Seats locked successfully for 10 minutes.")
                .build();
    }

    /**
     * Releases locks for a user.
     */
    public boolean unlockSeats(Long showId, String userId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) return true;

        for (Long seatId : seatIds) {
            String key = buildKey(showId, seatId);
            String val = redisTemplate.opsForValue().get(key);
            if (val != null && val.startsWith(userId + "|")) {
                redisTemplate.delete(key);
            }
        }
        return true;
    }

    /**
     * Checks if a seat is locked, and by whom.
     */
    public boolean isSeatLocked(Long showId, Long seatId) {
        String key = buildKey(showId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public String getSeatLockOwner(Long showId, Long seatId) {
        String key = buildKey(showId, seatId);
        String val = redisTemplate.opsForValue().get(key);
        if (val != null && val.contains("|")) {
            return val.split("\\|")[0];
        }
        return null;
    }
}

