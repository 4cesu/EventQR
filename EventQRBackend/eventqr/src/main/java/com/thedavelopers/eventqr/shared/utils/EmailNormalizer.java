package com.thedavelopers.eventqr.shared.utils;

/**
 * Canonicalizes email addresses for comparison and rate-limiting keys.
 *
 * <p>Beyond simple lower-casing, this collapses Gmail-style aliases so that distinct
 * spellings that land in the same inbox map to the same canonical key:
 * <ul>
 *   <li>lower-cases and trims the address,</li>
 *   <li>strips a leading {@code +tag} from the local part (email+bomb@... → email@...),</li>
 *   <li>for <em>gmail.com</em> / <em>googlemail.com</em> addresses, also strips dots in
 *       the local part (u.ser.name@ → username@).</li>
 * </ul>
 *
 * <p>This is used both by the forgot-password rate limiter (to prevent alias-based email
 * bombing from defeating the per-recipient budget) and by password-reset delivery, so the
 * rate-limit key always matches the actual recipient that receives the email.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    /**
     * Returns the canonical form used for rate-limit keys and recipient delivery,
     * or the trimmed/lower-cased input if it cannot be parsed (e.g. no {@code '@'}).
     */
    public static String canonicalize(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        int at = trimmed.indexOf('@');
        if (at < 0) {
            return trimmed;
        }
        String local = trimmed.substring(0, at);
        String domain = trimmed.substring(at + 1);

        // Strip a leading Gmail-style +tag from the local part.
        int plus = local.indexOf('+');
        if (plus >= 0) {
            local = local.substring(0, plus);
        }

        // For gmail/googlemail, dots in the local part are ignored by the mail server.
        if (domain.equals("gmail.com") || domain.equals("googlemail.com")) {
            local = local.replace(".", "");
        }

        return local + "@" + domain;
    }
}
