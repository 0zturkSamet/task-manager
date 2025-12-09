package com.taskmanager.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Security Audit Logging Service
 *
 * Provides centralized logging for security-related events.
 * All security events are logged with consistent formatting for SIEM integration.
 *
 * Events tracked:
 * - Authentication (login, logout, registration)
 * - Authorization failures
 * - Rate limiting triggers
 * - Suspicious activity detection
 * - Account modifications
 */
@Service
@Slf4j
public class SecurityAuditService {

    private static final String AUDIT_PREFIX = "[SECURITY_AUDIT]";

    /**
     * Log successful login
     */
    public void logSuccessfulLogin(String email, String ipAddress, String userAgent) {
        log.info("{} event=LOGIN_SUCCESS email={} ip={} userAgent={} timestamp={}",
                AUDIT_PREFIX,
                maskEmail(email),
                maskIpAddress(ipAddress),
                sanitizeUserAgent(userAgent),
                Instant.now()
        );
    }

    /**
     * Log failed login attempt
     */
    public void logFailedLogin(String email, String ipAddress, String reason) {
        log.warn("{} event=LOGIN_FAILED email={} ip={} reason={} timestamp={}",
                AUDIT_PREFIX,
                maskEmail(email),
                maskIpAddress(ipAddress),
                reason,
                Instant.now()
        );
    }

    /**
     * Log successful registration
     */
    public void logSuccessfulRegistration(UUID userId, String email, String ipAddress) {
        log.info("{} event=REGISTRATION_SUCCESS userId={} email={} ip={} timestamp={}",
                AUDIT_PREFIX,
                userId,
                maskEmail(email),
                maskIpAddress(ipAddress),
                Instant.now()
        );
    }

    /**
     * Log logout
     */
    public void logLogout(UUID userId, String ipAddress) {
        log.info("{} event=LOGOUT userId={} ip={} timestamp={}",
                AUDIT_PREFIX,
                userId,
                maskIpAddress(ipAddress),
                Instant.now()
        );
    }

    /**
     * Log authorization failure
     */
    public void logAuthorizationFailure(UUID userId, String resource, String action, String ipAddress) {
        log.warn("{} event=AUTHORIZATION_DENIED userId={} resource={} action={} ip={} timestamp={}",
                AUDIT_PREFIX,
                userId,
                resource,
                action,
                maskIpAddress(ipAddress),
                Instant.now()
        );
    }

    /**
     * Log rate limit triggered
     */
    public void logRateLimitTriggered(String ipAddress, String endpoint) {
        log.warn("{} event=RATE_LIMIT_TRIGGERED ip={} endpoint={} timestamp={}",
                AUDIT_PREFIX,
                maskIpAddress(ipAddress),
                endpoint,
                Instant.now()
        );
    }

    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String description, String ipAddress, String details) {
        log.warn("{} event=SUSPICIOUS_ACTIVITY description={} ip={} details={} timestamp={}",
                AUDIT_PREFIX,
                description,
                maskIpAddress(ipAddress),
                details,
                Instant.now()
        );
    }

    /**
     * Log account modification
     */
    public void logAccountModification(UUID userId, String modificationType, String ipAddress) {
        log.info("{} event=ACCOUNT_MODIFIED userId={} type={} ip={} timestamp={}",
                AUDIT_PREFIX,
                userId,
                modificationType,
                maskIpAddress(ipAddress),
                Instant.now()
        );
    }

    /**
     * Log password change
     */
    public void logPasswordChange(UUID userId, String ipAddress) {
        log.info("{} event=PASSWORD_CHANGED userId={} ip={} timestamp={}",
                AUDIT_PREFIX,
                userId,
                maskIpAddress(ipAddress),
                Instant.now()
        );
    }

    /**
     * Log potential XSS attempt detected
     */
    public void logXssAttemptDetected(String ipAddress, String field, String sanitizedValue) {
        log.warn("{} event=XSS_ATTEMPT_DETECTED ip={} field={} timestamp={}",
                AUDIT_PREFIX,
                maskIpAddress(ipAddress),
                field,
                Instant.now()
        );
    }

    /**
     * Log invalid JWT token
     */
    public void logInvalidToken(String ipAddress, String reason) {
        log.warn("{} event=INVALID_TOKEN ip={} reason={} timestamp={}",
                AUDIT_PREFIX,
                maskIpAddress(ipAddress),
                reason,
                Instant.now()
        );
    }

    /**
     * Mask email for privacy (show first char and domain)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    /**
     * Mask IP address for privacy (show first three octets)
     */
    private String maskIpAddress(String ip) {
        if (ip == null) {
            return "unknown";
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".xxx";
        }
        return "x.x.x.xxx";
    }

    /**
     * Sanitize user agent to prevent log injection
     */
    private String sanitizeUserAgent(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        // Remove newlines and limit length to prevent log injection
        return userAgent
                .replace("\n", "")
                .replace("\r", "")
                .substring(0, Math.min(userAgent.length(), 200));
    }
}
