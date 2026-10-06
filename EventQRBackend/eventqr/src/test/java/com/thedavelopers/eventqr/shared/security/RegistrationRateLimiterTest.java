package com.thedavelopers.eventqr.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrationRateLimiterTest {

    /** Mutable wall clock with fixed zone, controllable via {@link #advance}. */
    private static final class TestClock extends Clock {
        private final AtomicLong millis = new AtomicLong(1_000_000_000L);

        long advance(long ms) {
            return millis.addAndGet(ms);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis.get());
        }
    }

    @Mock
    private HttpServletRequest request;

    private TestClock clock;
    private RegistrationRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new TestClock();
        limiter = new RegistrationRateLimiter(clock, 10, 10);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.9");
        lenient().when(request.getRemoteAddr()).thenReturn("10.0.0.1");
    }

    @Test
    void burstAllowsTenThenRejectsEleventhWithinWindow() {
        for (int i = 0; i < 10; i++) {
            assertThat(limiter.allow(request, "attendee@example.com")).isTrue();
        }
        assertThat(limiter.allow(request, "attendee@example.com")).isFalse();
    }

    @Test
    void cooldownAfterWindowAllowsAgain() {
        for (int i = 0; i < 10; i++) {
            limiter.allow(request, "attendee@example.com");
        }
        assertThat(limiter.allow(request, "attendee@example.com")).isFalse();

        // Advance past the 60s window; the same IP/email should be allowed again.
        clock.advance(61_000L);
        assertThat(limiter.allow(request, "attendee@example.com")).isTrue();
    }

    @Test
    void perIpAndPerEmailBudgetsAreIndependent() {
        // Ten distinct emails from the same IP exhaust only the per-IP budget.
        for (int i = 0; i < 10; i++) {
            assertThat(limiter.allow(request, "attendee" + i + "@example.com")).isTrue();
        }
        // 11th request from the same IP (any email) is blocked by the per-IP budget.
        assertThat(limiter.allow(request, "attendee99@example.com")).isFalse();
    }

    @Test
    void differentIpHasIndependentBudget() {
        for (int i = 0; i < 10; i++) {
            limiter.allow(request, "attendee@example.com");
        }
        assertThat(limiter.allow(request, "attendee@example.com")).isFalse();

        // Another attendee with a different IP and a fresh email is allowed.
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.77");
        assertThat(limiter.allow(request, "other-attendee@example.com")).isTrue();
    }

    @Test
    void gmailAliasesShareOnePerEmailBudget() {
        String[] aliases = { "user+a@gmail.com", "user.b+c@gmail.com", "user+different@gmail.com",
                "u.s.e.r@gmail.com", "user+last@gmail.com", "user+sixth@gmail.com",
                "user+seventh@gmail.com", "user+eighth@gmail.com", "user+ninth@gmail.com",
                "user+tenth@gmail.com" };
        for (String alias : aliases) {
            assertThat(limiter.allow(request, alias)).isTrue();
        }
        // Alias 11th hits the email budget even though each alias string is distinct.
        assertThat(limiter.allow(request, "user+eleventh@gmail.com")).isFalse();
    }

    // ----- venue-sized limits (the production default is 60 per IP, 10 per email) -----

    @Test
    void aVenueSharingOneIpCanRegisterSixtyDifferentPeopleInAMinute() {
        RegistrationRateLimiter venue = new RegistrationRateLimiter(clock, 60, 10);

        for (int i = 0; i < 60; i++) {
            assertThat(venue.allow(request, "guest" + i + "@example.com")).as("guest %s", i).isTrue();
        }
        assertThat(venue.allow(request, "guest61@example.com")).isFalse();
    }

    @Test
    void theHigherIpLimitDoesNotLoosenThePerInboxLimit() {
        RegistrationRateLimiter venue = new RegistrationRateLimiter(clock, 60, 10);

        for (int i = 0; i < 10; i++) {
            assertThat(venue.allow(request, "victim@example.com")).isTrue();
        }
        assertThat(venue.allow(request, "victim@example.com")).isFalse();
        // other people on the same IP are unaffected
        assertThat(venue.allow(request, "someone-else@example.com")).isTrue();
    }

    @Test
    void aZeroOrNegativeSettingStillAllowsOneRequestInsteadOfBlockingEveryone() {
        RegistrationRateLimiter misconfigured = new RegistrationRateLimiter(clock, 0, -5);

        assertThat(misconfigured.allow(request, "a@example.com")).isTrue();
        assertThat(misconfigured.allow(request, "b@example.com")).isFalse();
    }
}
