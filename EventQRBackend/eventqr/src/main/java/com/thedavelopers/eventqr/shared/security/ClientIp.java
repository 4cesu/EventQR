package com.thedavelopers.eventqr.shared.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP for rate limiting and auditing.
 *
 * <p>The service runs behind Render's reverse proxy. A client can spoof its own
 * {@code X-Forwarded-For} value; a proxy that blindly trusts the leftmost entry would
 * let an attacker rotate the header per request to defeat IP-based rate limiting. To
 * close that bypass we take the <b>rightmost</b> syntactically valid entry in the
 * {@code X-Forwarded-For} list: each upstream hop appends the previous peer to the
 * right, so the rightmost client-visible entry is the one appended by the outermost
 * trusted proxy (Render) and is not client-controllable once we only trust the proxy.
 *
 * <p><b>Trust model:</b> we rely on the servlet container being configured with
 * {@code server.tomcat.remoteip.internal-proxies} (see {@code application.properties})
 * so that only Render's egress IPs are treated as trusted proxies whose
 * {@code X-Forwarded-For} we honor. If that list is empty the container trusts nothing
 * as a proxy; our header parsing alone must not blindly trust an arbitrary client.
 * This resolver is therefore defense in depth: it extracts the rightmost entry that a
 * trusted proxy appended, and falls back to {@code getRemoteAddr()} when there is no
 * valid forwarded address (e.g. direct local requests during development). Only
 * well-formed IPv4/IPv6 literals are accepted so a spoofed header value cannot produce
 * an arbitrary bucket key.
 */
public final class ClientIp {

    private static final int MAX_XFF_LENGTH = 512;

    private ClientIp() {
    }

    public static String from(HttpServletRequest request) {
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
