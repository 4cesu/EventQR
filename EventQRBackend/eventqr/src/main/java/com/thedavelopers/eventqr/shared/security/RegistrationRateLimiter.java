package com.thedavelopers.eventqr.shared.security;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.thedavelopers.eventqr.shared.utils.EmailNormalizer;

/**
 * Endpoint-specific rate limiter for {@code POST /api/v1/registrations}.
 *
 * <p>Registration is a public endpoint reachable without authentication, so the global
 * {@link RateLimitFilter} budget (200 requests per 10s per IP) alone lets an attacker
 * bulk-create accounts, triggering orphaned user_profiles and QR-email spam. This limiter
 * applies a much tighter sliding-window budget keyed by both the proxied-aware client IP
 * and the canonicalized attendee email, so abuse is throttled per source attacker and per
 * victim. The email key is canonicalized (see {@link EmailNormalizer}) so Gmail
 * {@code +tag}/dot aliases that land in the same inbox share one per-recipient budget.
 *
 * <p>Both limits must pass; a request is rejected as soon as either budget is exhausted.
 * State is in-memory and suitable for the single-instance deployment only.
 */
@Component
public class RegistrationRateLimiter {

    private static final long WINDOW_MS = 60_000L;

    private final Map<String, Deque<Long>> byIp = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> byEmail = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maxPerIp;
    private final int maxPerEmail;

    /**
     * @param maxPerIp    registrations allowed per client IP per minute. Everyone on one venue Wi-Fi
     *                    shares an IP, so this must cover a whole crowd signing up at once.
     * @param maxPerEmail registrations allowed per (canonicalized) inbox per minute; this is what
     *                    stops one victim being flooded with QR emails, so it stays small.
     */
    @Autowired
    public RegistrationRateLimiter(@Value("${app.rate-limit.registration.per-ip:60}") int maxPerIp,
                                   @Value("${app.rate-limit.registration.per-email:10}") int maxPerEmail) {
        this(Clock.systemUTC(), maxPerIp, maxPerEmail);
    }

    /** Package-private constructor for tests that need clock control. */
    RegistrationRateLimiter(Clock clock, int maxPerIp, int maxPerEmail) {
        this.clock = clock;
        // A misconfigured 0 or negative value must not silently block every registration.
        this.maxPerIp = Math.max(1, maxPerIp);
        this.maxPerEmail = Math.max(1, maxPerEmail);
    }

    /**
     * Returns true if the request should be allowed, otherwise false (caller returns 429).
     * Records the attempt under both keys when allowed relative to each individual budget.
     */
    public boolean allow(HttpServletRequest request, String email) {
        String ip = ClientIp.from(request);
        String canonicalEmail = email == null ? "" : EmailNormalizer.canonicalize(email);
        if (!withinBudget(byIp, ip, maxPerIp)) {
            return false;
        }
        if (!canonicalEmail.isEmpty() && !withinBudget(byEmail, canonicalEmail, maxPerEmail)) {
            return false;
        }
        record(byIp, ip);
        if (!canonicalEmail.isEmpty()) {
            record(byEmail, canonicalEmail);
        }
        return true;
    }

    private boolean withinBudget(Map<String, Deque<Long>> window, String key, int max) {
        Deque<Long> deque = window.computeIfAbsent(key, k -> new ArrayDeque<>());
        long now = clock.millis();
        synchronized (deque) {
            prune(deque, now);
            return deque.size() < max;
        }
    }

    private void record(Map<String, Deque<Long>> window, String key) {
        Deque<Long> deque = window.computeIfAbsent(key, k -> new ArrayDeque<>());
        long now = clock.millis();
        synchronized (deque) {
            prune(deque, now);
            deque.addLast(now);
        }
    }

    private void prune(Deque<Long> deque, long now) {
        while (!deque.isEmpty() && deque.peekFirst() < now - WINDOW_MS) {
            deque.removeFirst();
        }
    }
}
