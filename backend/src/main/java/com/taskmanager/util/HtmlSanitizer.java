package com.taskmanager.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for sanitizing HTML input to prevent XSS attacks.
 * Uses OWASP Java HTML Sanitizer.
 */
@Component
public class HtmlSanitizer {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            // Allow basic text formatting
            .allowElements("b", "i", "u", "em", "strong", "br", "p")
            // No attributes allowed to prevent javascript: and data: URIs
            .toFactory();

    /**
     * Sanitizes HTML input by removing potentially dangerous content.
     * @param input The input string that may contain HTML
     * @return Sanitized string safe for storage and display
     */
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return POLICY.sanitize(input);
    }

    /**
     * Sanitizes input and strips all HTML tags.
     * @param input The input string
     * @return Plain text without any HTML
     */
    public String sanitizeToPlainText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Strip all HTML tags
        return input.replaceAll("<[^>]*>", "");
    }
}
