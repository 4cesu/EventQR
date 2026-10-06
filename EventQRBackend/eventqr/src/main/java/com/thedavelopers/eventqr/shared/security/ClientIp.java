package com.thedavelopers.eventqr.shared.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP for rate limiting and auditing.
 *
 * <p>Resolution order:
 * <ol>
 *   <li><b>A trusted single-value header</b>, when one is configured
 *       ({@code app.client-ip.trusted-header}, {@code CF-Connecting-IP} on Render). Render's edge
 *       (Cloudflare) writes that header on every request and overwrites whatever the caller
 *       sent, so it is accurate and the client cannot forge it. Only use this where the platform
 *       really guarantees that; on a host without such an edge a caller could set it themselves,
 *       so it can be turned off by setting the property empty.</li>
 *   <li>The <b>rightmost</b> valid entry of {@code X-Forwarded-For}. A client can spoof its own
 *       value, and every hop appends on the right, so the leftmost entries must never be
 *       trusted; the rightmost is the peer seen by the outermost proxy.</li>
 *   <li>{@code getRemoteAddr()} when there is no valid forwarded address (e.g. local
 *       development).</li>
 * </ol>
 *
 * <p>Only well-formed IPv4/IPv6 literals are accepted at every step, so a spoofed header value
 * cannot produce an arbitrary rate-limit bucket key.
 *
 * <p>This replaces the need for {@code server.tomcat.remoteip.internal-proxies}: Render does not
 * publish its inbound proxy ranges, so there is no list to configure.
 */
public final class ClientIp {

    private static final int MAX_XFF_LENGTH = 512;

    /** Name of the header the platform guarantees to be the real client IP; null/blank = not used. */
    private static volatile String trustedHeader;

    private ClientIp() {
    }

    /** Set once at startup from {@code app.client-ip.trusted-header}; blank disables it. */
    public static void configureTrustedHeader(String headerName) {
        trustedHeader = (headerName == null || headerName.isBlank()) ? null : headerName.trim();
    }

    public static String from(HttpServletRequest request) {
        String header = trustedHeader;
        if (header != null) {
            String value = request.getHeader(header);
            if (value != null && isValidIp(value.trim())) {
                return value.trim();
            }
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String candidate = rightmostValidAddress(forwarded);
        if (candidate != null) {
            return candidate;
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "unknown";
    }

    /**
     * Returns the last syntactically valid IP in the given X-Forwarded-For list, or
     * {@code null} if there is none. The rightmost entry is the peer of the outermost
     * trusted proxy (Render) and thus the real client; earlier entries may be spoofed by
     * the client itself and are intentionally ignored.
     */
    private static String rightmostValidAddress(String xForwardedFor) {
        if (xForwardedFor == null || xForwardedFor.isBlank() || xForwardedFor.length() > MAX_XFF_LENGTH) {
            return null;
        }
        String[] parts = xForwardedFor.split(",");
        for (int i = parts.length - 1; i >= 0; i--) {
            String candidate = parts[i].trim();
            if (isValidIp(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isValidIp(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.indexOf(':') >= 0 ? isValidIpv6(value) : isValidIpv4(value);
    }

    private static boolean isValidIpv4(String value) {
        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            if (!isNumeric(octet)) {
                return false;
            }
            int parsed = Integer.parseInt(octet);
            if (parsed < 0 || parsed > 255) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidIpv6(String value) {
        long count = value.chars().filter(c -> c == ':').count();
        if (count < 2 || count > 8) {
            return false;
        }
        final String candidate = value.endsWith(":") || value.startsWith(":") ? ":" + value.replace("::", ":") + ":" : value;
        for (String group : candidate.split(":", -1)) {
            if (group.isEmpty()) {
                continue;
            }
            if (group.length() > 4 || !group.matches("[0-9a-fA-F]{1,4}")) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNumeric(String value) {
        if (value.isEmpty() || value.length() > 3) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
