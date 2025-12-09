package com.taskmanager.security;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * XSS Sanitization Utility
 *
 * Provides methods to sanitize user input and prevent Cross-Site Scripting (XSS) attacks.
 * This utility should be used to clean all user-provided text that will be stored or displayed.
 *
 * Security features:
 * - HTML entity encoding
 * - Script tag removal
 * - Event handler attribute removal
 * - JavaScript URL protocol removal
 */
@Component
public class XssSanitizer {

    // Pattern to match script tags and their content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Pattern to match event handlers (onclick, onerror, onload, etc.)
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "\\s*on\\w+\\s*=\\s*([\"'][^\"']*[\"']|[^\\s>]+)",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to match javascript: and data: URLs
    private static final Pattern DANGEROUS_PROTOCOL_PATTERN = Pattern.compile(
            "(javascript|data|vbscript)\\s*:",
            Pattern.CASE_INSENSITIVE
    );

    // Pattern to match style expressions (expression(), url(), etc.)
    private static final Pattern STYLE_EXPRESSION_PATTERN = Pattern.compile(
            "(expression|url)\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Sanitize input string to prevent XSS attacks.
     * Uses HTML entity encoding as the primary defense.
     *
     * @param input The potentially unsafe input string
     * @return Sanitized string safe for display
     */
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // First, remove any script tags
        String cleaned = SCRIPT_PATTERN.matcher(input).replaceAll("");

        // Remove event handlers
        cleaned = EVENT_HANDLER_PATTERN.matcher(cleaned).replaceAll("");

        // Remove dangerous protocols
        cleaned = DANGEROUS_PROTOCOL_PATTERN.matcher(cleaned).replaceAll("");

        // Remove style expressions
        cleaned = STYLE_EXPRESSION_PATTERN.matcher(cleaned).replaceAll("");

        // HTML encode the result
        return HtmlUtils.htmlEscape(cleaned);
    }

    /**
     * Sanitize input for strict text-only fields (names, titles, etc.)
     * Removes all HTML tags and encodes special characters.
     *
     * @param input The potentially unsafe input string
     * @return Sanitized plain text string
     */
    public String sanitizeStrict(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove all HTML tags
        String cleaned = input.replaceAll("<[^>]*>", "");

        // HTML encode the result
        return HtmlUtils.htmlEscape(cleaned);
    }

    /**
     * Check if input contains potentially dangerous content.
     * Useful for logging/alerting purposes.
     *
     * @param input The input string to check
     * @return true if potentially dangerous content is detected
     */
    public boolean containsDangerousContent(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        return SCRIPT_PATTERN.matcher(input).find() ||
                EVENT_HANDLER_PATTERN.matcher(input).find() ||
                DANGEROUS_PROTOCOL_PATTERN.matcher(input).find() ||
                STYLE_EXPRESSION_PATTERN.matcher(input).find();
    }

    /**
     * Sanitize for use in JSON strings.
     * Escapes characters that could break JSON or enable injection.
     *
     * @param input The input string
     * @return JSON-safe string
     */
    public String sanitizeForJson(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
