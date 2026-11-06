package com.ticketflow.booking.redis;

import com.ticketflow.common.dto.SeatLockRequest;
import com.ticketflow.common.dto.SeatLockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSeatLockService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> releaseLockScript;

    private static final String LOCK_PREFIX = "lock:show:%d:seat:%d";
    private static final String USER_LOCKS_PREFIX = "user:locks:%s:show:%d";

    private String buildKey(Long showId, Long seatId) {
        return String.format(LOCK_PREFIX, showId, seatId);
    }

    private String buildUserLocksKey(String userId, Long showId) {
        return String.format(USER_LOCKS_PREFIX, userId, showId);
    }

    /**
     * Atomically acquires distributed locks for all requested seats.
     * If any seat is already locked by another user, roll back all locks acquired
     * in this batch.
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

        // If any failed, rollback all acquired seats in this attempt using atomic
        // release
        if (!failedSeatIds.isEmpty()) {
            for (Long acquiredSeatId : successfullyLocked) {
                String key = buildKey(showId, acquiredSeatId);
                redisTemplate.execute(releaseLockScript, Collections.singletonList(key), lockValue);
            }
            return SeatLockResponse.builder()
                    .locked(false)
                    .showId(showId)
                    .lockedSeatIds(Collections.emptyList())
                    .failedSeatIds(failedSeatIds)
                    .message("One or more seats are currently locked by another user. Please choose different seats.")
                    .build();
        }

        // Maintain reverse index: user:locks:{userId}:show:{showId} set
        if (userId != null && !userId.isBlank()) {
            String userLocksKey = buildUserLocksKey(userId, showId);
            String[] seatIdStrings = successfullyLocked.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(userLocksKey, seatIdStrings);
            redisTemplate.expire(userLocksKey, Duration.ofSeconds(ttlSeconds));
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
     * Releases locks for a user safely.
     * Uses atomic Lua script to ensure we only delete keys belonging to this user.
     */
    public boolean unlockSeats(Long showId, String userId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty())
            return true;

        String userLocksKey = (userId != null && !userId.isBlank()) ? buildUserLocksKey(userId, showId) : null;

        for (Long seatId : seatIds) {
            String key = buildKey(showId, seatId);
            String currentVal = redisTemplate.opsForValue().get(key);
            if (currentVal != null) {
                if (userId == null || userId.isBlank() || currentVal.startsWith(userId + "|")) {
                    redisTemplate.execute(releaseLockScript, Collections.singletonList(key), currentVal);
                }
            }
            if (userLocksKey != null) {
                redisTemplate.opsForSet().remove(userLocksKey, String.valueOf(seatId));
            }
        }
        return true;
    }

    /**
     * Unlocks all seats currently locked by a given user for a show.
     */
    public boolean unlockAllUserSeats(Long showId, String userId) {
        if (userId == null || userId.isBlank()) return true;

        String userLocksKey = buildUserLocksKey(userId, showId);
        Set<String> lockedSeats = redisTemplate.opsForSet().members(userLocksKey);
        if (lockedSeats != null && !lockedSeats.isEmpty()) {
            List<Long> seatIds = lockedSeats.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            return unlockSeats(showId, userId, seatIds);
        }
        return true;
    }

    /**
     * Checks if a seat is locked.
     */
    public boolean isSeatLocked(Long showId, Long seatId) {
        String key = buildKey(showId, seatId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Extracts the userId who owns the lock on a seat.
     */
    public String getSeatLockOwner(Long showId, Long seatId) {
        String key = buildKey(showId, seatId);
        String val = redisTemplate.opsForValue().get(key);
        if (val != null && val.contains("|")) {
            return val.split("\\|")[0];
        }
        return null;
    }

    /**
     * Returns remaining TTL in seconds for a seat lock.
     */
    public Long getSeatLockTtlSeconds(Long showId, Long seatId) {
        String key = buildKey(showId, seatId);
        return redisTemplate.getExpire(key);
    }
}
