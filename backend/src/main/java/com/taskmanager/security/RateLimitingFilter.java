package com.taskmanager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter to prevent brute force attacks on authentication endpoints.
 * Limits: 5 requests per 15 minutes per IP address for auth endpoints.
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Apply rate limiting only to authentication endpoints
        if (requestURI.startsWith("/api/auth/login") ||
            requestURI.startsWith("/api/auth/register")) {

            String clientIP = getClientIP(request);
            Bucket bucket = resolveBucket(clientIP);

            if (bucket.tryConsume(1)) {
                // Request allowed
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIP, requestURI);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            }
        } else {
            // No rate limiting for other endpoints
            filterChain.doFilter(request, response);
        }
    }

    private Bucket resolveBucket(String clientIP) {
        return cache.computeIfAbsent(clientIP, key -> createNewBucket());
    }

    private Bucket createNewBucket() {
        // Allow 5 requests per 15 minutes
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        // Try to get real IP from proxy headers
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}
