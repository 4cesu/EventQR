package com.thedavelopers.eventqr.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thedavelopers.eventqr.shared.constants.AccountRole;

/**
 * The denylist must outlive a single JwtService instance: a restart or a second server
 * instance has a fresh in-memory cache but shares the database-backed store.
 */
class JwtServiceDurableDenylistTest {

    private static final String SECRET = "01234567890123456789012345678901"; // 32 bytes (HS256 min)

    private final Map<String, Instant> rows = new HashMap<>();
    private int lookups;

    private final RevokedTokenStore store = new RevokedTokenStore() {
        @Override
        public void save(String tokenHash, Instant expiresAt) {
            rows.put(tokenHash, expiresAt);
        }

        @Override
        public Optional<Instant> findActive(String tokenHash) {
            lookups++;
            return Optional.ofNullable(rows.get(tokenHash)).filter(at -> at.isAfter(Instant.now()));
        }
    };

    private JwtService newInstance() {
        JwtService service = new JwtService(SECRET, 86_400_000L);
        service.setRevokedTokenStore(store);
        return service;
    }

    private String bearer(JwtService service) {
        return "Bearer " + service.createToken(UUID.randomUUID(), "user@example.com", AccountRole.ATTENDEE);
    }

    @BeforeEach
    void reset() {
        rows.clear();
        lookups = 0;
    }

    @Test
    void logoutSurvivesRestart() {
        JwtService beforeRestart = newInstance();
        String bearer = bearer(beforeRestart);
        beforeRestart.revoke(bearer);

        JwtService afterRestart = newInstance();

        assertThat(afterRestart.isRevoked(bearer)).isTrue();
    }

    @Test
    void logoutOnOneInstanceIsSeenByAnother() {
        JwtService instanceA = newInstance();
        JwtService instanceB = newInstance();
        String bearer = bearer(instanceA);

        instanceA.revoke(bearer);

        assertThat(instanceB.isRevoked(bearer)).isTrue();
    }

    @Test
    void storedValueIsAHashNeverTheToken() {
        JwtService service = newInstance();
        String bearer = bearer(service);

        service.revoke(bearer);

        String token = bearer.substring("Bearer ".length());
        assertThat(rows).hasSize(1);
        assertThat(rows.keySet().iterator().next()).hasSize(64).doesNotContain(token);
    }

    @Test
    void unrevokedTokenIsCheckedOnceThenServedFromCache() {
        JwtService service = newInstance();
        String bearer = bearer(service);

        assertThat(service.isRevoked(bearer)).isFalse();
        assertThat(service.isRevoked(bearer)).isFalse();
        assertThat(service.isRevoked(bearer)).isFalse();

        assertThat(lookups).isEqualTo(1);
    }

    @Test
    void revokingAfterACachedNotRevokedResultTakesEffectImmediately() {
        JwtService service = newInstance();
        String bearer = bearer(service);
        assertThat(service.isRevoked(bearer)).isFalse();

        service.revoke(bearer);

        assertThat(service.isRevoked(bearer)).isTrue();
    }
}
