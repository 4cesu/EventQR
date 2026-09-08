package com.thedavelopers.eventqr.shared.utils;

/**
 * Helpers for redacting PII in application logs.
 */
public final class LogRedaction {

    private LogRedaction() {
    }

    /**
     * Masks an email address for safe logging, e.g. {@code a***@example.com}.
     * Preserves the domain so the log stays useful for debugging while hiding
     * the account identifier. A null/blank/illegal value passes through unchanged.
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 0 || at == trimmed.length() - 1) {
            return trimmed;
        }
        String local = trimmed.substring(0, at);
        String domain = trimmed.substring(at + 1);
        String maskedLocal = local.charAt(0) + "***";
        return maskedLocal + "@" + domain;
    }
}
