package com.thedavelopers.eventqr.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;

import jakarta.servlet.http.HttpServletRequest;

/** The property names in application.properties must really reach the limiter. */
class RegistrationRateLimiterWiringTest {

    private RegistrationRateLimiter build(Map<String, Object> properties) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
        context.register(PropertySourcesPlaceholderConfigurer.class, RegistrationRateLimiter.class);
        context.refresh();
        return context.getBean(RegistrationRateLimiter.class);
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.9");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        return request;
    }

    private int accepted(RegistrationRateLimiter limiter, int attempts) {
        HttpServletRequest request = request();
        int ok = 0;
        for (int i = 0; i < attempts; i++) {
            if (limiter.allow(request, "guest" + i + "@example.com")) {
                ok++;
            }
        }
        return ok;
    }

    @Test
    void theDefaultsAreSixtyPerIp() {
        assertThat(accepted(build(Map.of()), 100)).isEqualTo(60);
    }

    @Test
    void theConfiguredPerIpLimitIsUsed() {
        RegistrationRateLimiter limiter = build(Map.of("app.rate-limit.registration.per-ip", "25"));

        assertThat(accepted(limiter, 100)).isEqualTo(25);
    }

    @Test
    void theConfiguredPerEmailLimitIsUsed() {
        RegistrationRateLimiter limiter = build(Map.of("app.rate-limit.registration.per-email", "2"));
        HttpServletRequest request = request();

        assertThat(limiter.allow(request, "same@example.com")).isTrue();
        assertThat(limiter.allow(request, "same@example.com")).isTrue();
        assertThat(limiter.allow(request, "same@example.com")).isFalse();
    }
}
