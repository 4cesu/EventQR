package com.thedavelopers.eventqr.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thedavelopers.eventqr.shared.constants.AccountRole;

class JwtServiceUniqueTokenTest {

    private static final String SECRET = "01234567890123456789012345678901"; // 32 bytes (HS256 min)

    @Test
    void twoTokensForTheSameUserInTheSameSecondAreDifferent() {
        JwtService service = new JwtService(SECRET, 3_600_000L);
        UUID userId = UUID.randomUUID();

        String first = service.createToken(userId, "user@example.com", AccountRole.ATTENDEE);
        String second = service.createToken(userId, "user@example.com", AccountRole.ATTENDEE);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void loggingOutThenBackInImmediatelyDoesNotReturnARevokedToken() {
        JwtService service = new JwtService(SECRET, 3_600_000L);
        UUID userId = UUID.randomUUID();
        String firstBearer = "Bearer " + service.createToken(userId, "user@example.com", AccountRole.ATTENDEE);
        service.revoke(firstBearer);

        String secondBearer = "Bearer " + service.createToken(userId, "user@example.com", AccountRole.ATTENDEE);

        assertThat(service.isRevoked(firstBearer)).isTrue();
        assertThat(service.isRevoked(secondBearer)).isFalse();
    }
}
