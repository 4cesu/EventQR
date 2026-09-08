package com.thedavelopers.eventqr.shared.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter implements Filter {

    private static final int MAX_PER_IP = 200;
    private static final long WINDOW_MS = 10_000;

    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private long windowStart = System.currentTimeMillis();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String key = request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
        long now = System.currentTimeMillis();
        if (now - windowStart > WINDOW_MS) {
            synchronized (this) {
                if (now - windowStart > WINDOW_MS) {
                    counts.clear();
                    windowStart = now;
                }
            }
        }
        AtomicInteger counter = counts.computeIfAbsent(key, k -> new AtomicInteger(0));
        int current = counter.incrementAndGet();
        if (current > MAX_PER_IP) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Rate limited\"}");
            response.setContentType("application/json");
            return; // load shedding: 429 not 500
        }
        chain.doFilter(req, res);
    }
}
