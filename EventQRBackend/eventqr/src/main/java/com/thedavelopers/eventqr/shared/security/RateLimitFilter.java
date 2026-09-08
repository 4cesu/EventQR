package com.thedavelopers.eventqr.shared.security;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.thedavelopers.eventqr.shared.response.ErrorResponse;

/**
 * Global per-IP rate limiter using a token bucket.
 *
 * <p>The bucket holds up to {@value #BURST} tokens and refills at
 * {@value #RATE_PER_SECOND} tokens/second (equivalent to the legacy limit of
 * 200 requests per 10s). Unlike the previous fixed-window counter that cleared on
 * every window reset, a token bucket does not let a client amortize two full
 * windows' worth of requests across the boundary (~2x bypass), because the bucket
 * only ever refills at a steady rate and never jumps back to a full capacity.
 *
 * <p>The bucket is keyed by the proxied-aware client IP (see {@link ClientIp}) so
 * requests behind Render's reverse proxy are bucketed per real client rather than
 * per proxy IP.
 *
 * <p>Bucket state is kept in-memory and is therefore suitable for the single-instance
 * deployment only.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter implements Filter {

    private static final int BURST = 200;
    private static final double RATE_PER_SECOND = BURST / 10.0; // 200 / 10s
    private static final int MAX_KEYS = 10_000;
    private static final Duration IDLE_EXPIRY = Duration.ofMinutes(5);

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(MAX_KEYS)
            .expireAfterAccess(IDLE_EXPIRY)
            .build();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String key = ClientIp.from(request);

        long now = System.currentTimeMillis();
        Bucket bucket = buckets.get(key, k -> Bucket.empty(now));
        bucket.refill(now);
        if (!bucket.tryConsume()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            // Mirror the standard ErrorResponse envelope used by GlobalExceptionHandler so
            // mobile/client error parsing stays consistent across all endpoints.
            response.setCharacterEncoding("UTF-8");
            String body = new ErrorResponse(java.time.Instant.now(),
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                    "Rate limit exceeded. Please try again later.",
                    request.getRequestURI())
                    .toJson();
            response.getWriter().write(body);
            return; // load shedding: 429 not 500
        }
        chain.doFilter(req, res);
    }

    /**
     * Thread-safe token bucket. {@code tokens} can exceed capacity transiently and is
     * capped at {@code BURST}; {@code refill} is atomic to keep decrement and refill
     * consistent across concurrent requests.
     */
    static final class Bucket {
        private final AtomicLong tokensMillis;
        private volatile long lastRefillAt;

        Bucket(double tokens, long now) {
            this.tokensMillis = new AtomicLong(Math.round(tokens * 1000));
            this.lastRefillAt = now;
        }

        static Bucket empty(long now) {
            return new Bucket(BURST, now);
        }

        /** @return current tokens expressed in milli-tokens (tokens * 1000), for tests/observability. */
        long milliTokens() {
            return tokensMillis.get();
        }

        /**
         * Add tokens accrued since the last refill (capped at capacity).
         *
         * <p>Synchronized so that {@code lastRefillAt} and the token accumulation form
         * one critical section: without this, two concurrent refills could both read the
         * same {@code lastRefillAt}, both credit the same elapsed window, and double-count
         * the refill (~2x the intended rate). Serializing the refill is cheap per request
         * and keeps the accumulate-and-cap CAS loop intact for the token update itself.
         * {@code tryConsume} stays lock-free (it only touches the AtomicLong).
         */
        synchronized void refill(long now) {
            long elapsedMs = now - lastRefillAt;
            if (elapsedMs <= 0) {
                return;
            }
            lastRefillAt = now;
            long added = Math.round(RATE_PER_SECOND * elapsedMs);
            // Atomic accumulate-and-cap: only ever increase tokens toward BURST.
            // tokensMillis holds milli-tokens (tokens * 1000), so the cap must be
            // BURST * 1000 to preserve full burst capacity (200 tokens = 200,000 milli-tokens).
            while (true) {
                long current = tokensMillis.get();
                long target = Math.min(BURST * 1000L, current + added);
                if (tokensMillis.compareAndSet(current, target)) {
                    break;
                }
            }
        }

        /** Consume one token if available; returns false when the bucket is empty. */
        boolean tryConsume() {
            while (true) {
                long current = tokensMillis.get();
                if (current <= 0) {
                    return false;
                }
                if (tokensMillis.compareAndSet(current, current - 1000L)) {
                    return true;
                }
            }
        }
    }
}
