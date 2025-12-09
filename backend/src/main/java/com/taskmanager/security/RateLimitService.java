package com.taskmanager.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Service
 *
 * Implements a sliding window rate limiter to prevent brute force attacks.
 * Uses in-memory storage with automatic cleanup of expired entries.
 *
 * Security features:
 * - Limits login attempts per IP address
 * - Limits registration attempts per IP address
 * - Automatic lockout after exceeding limits
 * - Cleanup of stale entries to prevent memory exhaustion
 */
@Service
@Slf4j
public class RateLimitService {

    // Configuration constants
    private static final int MAX_LOGIN_ATTEMPTS = 5;          // Max login attempts per window
    private static final int MAX_REGISTER_ATTEMPTS = 3;       // Max registration attempts per window
    private static final int WINDOW_SIZE_SECONDS = 300;       // 5-minute window
    private static final int LOCKOUT_DURATION_SECONDS = 900;  // 15-minute lockout after exceeding

    // Storage for attempt tracking
    private final Map<String, AttemptInfo> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, AttemptInfo> registerAttempts = new ConcurrentHashMap<>();

    public RateLimitService() {
        // Schedule cleanup every 5 minutes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Check if login is allowed for the given IP
     * @return true if allowed, false if rate limited
     */
    public boolean isLoginAllowed(String ipAddress) {
        return isAllowed(ipAddress, loginAttempts, MAX_LOGIN_ATTEMPTS, "login");
    }

    /**
     * Check if registration is allowed for the given IP
     * @return true if allowed, false if rate limited
     */
    public boolean isRegistrationAllowed(String ipAddress) {
        return isAllowed(ipAddress, registerAttempts, MAX_REGISTER_ATTEMPTS, "registration");
    }

    /**
     * Record a failed login attempt
     */
    public void recordFailedLogin(String ipAddress) {
        recordAttempt(ipAddress, loginAttempts);
        log.warn("Failed login attempt recorded for IP: {}", maskIpAddress(ipAddress));
    }

    /**
     * Record a registration attempt
     */
    public void recordRegistrationAttempt(String ipAddress) {
        recordAttempt(ipAddress, registerAttempts);
    }

    /**
     * Clear login attempts for IP (call on successful login)
     */
    public void clearLoginAttempts(String ipAddress) {
        loginAttempts.remove(ipAddress);
    }

    /**
     * Get remaining time until rate limit reset (in seconds)
     */
    public long getRemainingLockoutTime(String ipAddress) {
        AttemptInfo info = loginAttempts.get(ipAddress);
        if (info == null || !info.isLockedOut()) {
            return 0;
        }
        return Math.max(0, info.lockoutUntil - Instant.now().getEpochSecond());
    }

    private boolean isAllowed(String ipAddress, Map<String, AttemptInfo> attempts,
                              int maxAttempts, String type) {
        AttemptInfo info = attempts.get(ipAddress);

        if (info == null) {
            return true;
        }

        // Check if currently locked out
        if (info.isLockedOut()) {
            log.warn("Rate limit lockout active for {} from IP: {}", type, maskIpAddress(ipAddress));
            return false;
        }

        // Check if within window and exceeded attempts
        if (info.isWithinWindow() && info.attemptCount >= maxAttempts) {
            // Trigger lockout
            info.lockoutUntil = Instant.now().getEpochSecond() + LOCKOUT_DURATION_SECONDS;
            log.warn("Rate limit exceeded for {} from IP: {}. Lockout initiated.",
                    type, maskIpAddress(ipAddress));
            return false;
        }

        return true;
    }

    private void recordAttempt(String ipAddress, Map<String, AttemptInfo> attempts) {
        attempts.compute(ipAddress, (key, existing) -> {
            if (existing == null || !existing.isWithinWindow()) {
                // Start new window
                return new AttemptInfo(1, Instant.now().getEpochSecond(), 0);
            } else {
                // Increment existing
                existing.attemptCount++;
                return existing;
            }
        });
    }

    private void cleanupExpiredEntries() {
        long now = Instant.now().getEpochSecond();
        long threshold = now - WINDOW_SIZE_SECONDS - LOCKOUT_DURATION_SECONDS;

        loginAttempts.entrySet().removeIf(e ->
                e.getValue().windowStart < threshold && !e.getValue().isLockedOut());
        registerAttempts.entrySet().removeIf(e ->
                e.getValue().windowStart < threshold && !e.getValue().isLockedOut());

        log.debug("Rate limit cleanup completed. Login entries: {}, Register entries: {}",
                loginAttempts.size(), registerAttempts.size());
    }

    /**
     * Mask IP address for logging (privacy protection)
     */
    private String maskIpAddress(String ip) {
        if (ip == null) return "unknown";
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".xxx";
        }
        return "x.x.x.xxx";
    }

    /**
     * Internal class to track attempt information
     */
    private static class AttemptInfo {
        int attemptCount;
        long windowStart;
        long lockoutUntil;

        AttemptInfo(int attemptCount, long windowStart, long lockoutUntil) {
            this.attemptCount = attemptCount;
            this.windowStart = windowStart;
            this.lockoutUntil = lockoutUntil;
        }

        boolean isWithinWindow() {
            return Instant.now().getEpochSecond() - windowStart < WINDOW_SIZE_SECONDS;
        }

        boolean isLockedOut() {
            return lockoutUntil > Instant.now().getEpochSecond();
        }
    }
}
