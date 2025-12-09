package com.taskmanager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security Headers Configuration
 *
 * Adds essential HTTP security headers to prevent common web vulnerabilities:
 * - X-Content-Type-Options: Prevents MIME-sniffing attacks
 * - X-Frame-Options: Prevents clickjacking attacks
 * - X-XSS-Protection: Legacy XSS filter (for older browsers)
 * - Strict-Transport-Security: Enforces HTTPS connections
 * - Content-Security-Policy: Restricts resource loading
 * - Referrer-Policy: Controls referrer information leakage
 * - Permissions-Policy: Restricts browser features
 */
@Configuration
public class SecurityHeadersConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
            ) throws ServletException, IOException {

                // Prevent MIME type sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");

                // Prevent clickjacking - deny all framing
                response.setHeader("X-Frame-Options", "DENY");

                // Legacy XSS protection for older browsers
                response.setHeader("X-XSS-Protection", "1; mode=block");

                // Only add HSTS in production (requires HTTPS)
                if ("prod".equals(activeProfile)) {
                    // Enforce HTTPS for 1 year, include subdomains
                    response.setHeader("Strict-Transport-Security",
                            "max-age=31536000; includeSubDomains; preload");
                }

                // Content Security Policy - restrictive default
                response.setHeader("Content-Security-Policy",
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'; " +
                        "base-uri 'self'");

                // Control referrer information
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                // Restrict browser features/APIs
                response.setHeader("Permissions-Policy",
                        "geolocation=(), microphone=(), camera=(), payment=()");

                // Prevent caching of sensitive data
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                response.setHeader("Pragma", "no-cache");

                filterChain.doFilter(request, response);
            }
        };
    }
}
