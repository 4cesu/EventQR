package com.thedavelopers.eventqr.shared.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.thedavelopers.eventqr.features.users.model.entity.UserTokenRevocation;
import com.thedavelopers.eventqr.features.users.repository.UserProfileRepository;
import com.thedavelopers.eventqr.features.users.repository.UserTokenRevocationRepository;
import com.thedavelopers.eventqr.shared.constants.AccountStatus;

/**
 * DB-backed account-access gate used by {@link JwtAuthenticationFilter}.
 *
 * <p>Decision inputs — all sourced from the database, never from per-JVM memory:
 * <ul>
 *   <li>the user's current {@code status} (only ACTIVE may authenticate), and</li>
 *   <li>the {@code revoked_at} marker from {@code user_token_revocation} if the
 *       account was ever disabled/suspended.</li>
 * </ul>
 * A token is rejected when the account is not ACTIVE or when the token's
 * {@code iat} is not strictly after {@code revoked_at} (i.e. tokens issued before the
 * disable stay dead even after the account is re-enabled — no resurrection).
 *
 * <p><b>Caching:</b> the two single-row primary-key lookups are cached per JVM for
 * 30 seconds ({@link #CACHE_TTL}). Consequences:
 * <ul>
 *   <li>a disable/suspend propagates to other instances within at most ~TTL, and</li>
 *   <li>a re-enable also propagates within ~TTL, so a freshly-logged-in user may still
 *       be rejected for up to the TTL after re-enable (brief, bounded, safe-by-default:
 *       the DB row is kept, and login/refresh issue a token only after the re-enable).</li>
 * </ul>
 */
@Component
public class UserTokenRevocationChecker {

    /** Propagation window for status/revocation changes across instances. */
    static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private static final int MAX_CACHE_ENTRIES = 100_000;

    private record AccountAccess(AccountStatus status, Instant revokedAt) {
    }

    private final UserProfileRepository userProfileRepository;
    private final UserTokenRevocationRepository userTokenRevocationRepository;

    private final Cache<UUID, AccountAccess> accessCache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_ENTRIES)
            .expireAfterWrite(CACHE_TTL)
            .build();

    public UserTokenRevocationChecker(UserProfileRepository userProfileRepository,
                                      UserTokenRevocationRepository userTokenRevocationRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userTokenRevocationRepository = userTokenRevocationRepository;
    }

    /**
     * @return {@code true} when the user may act on the presented token: account is
     * ACTIVE and the token was issued strictly after the last disable/suspend.
     */
    public boolean isAccessAllowed(UUID userId, Instant tokenIssuedAt) {
        if (tokenIssuedAt == null) {
            // Tokens minted by this application always carry 'iat'; a missing one is
            // treated as revoked rather than risk resurrecting a pre-disable session.
            return false;
        }
        AccountAccess access = accessCache.get(userId, this::loadAccountAccess);
        if (access == null || access.status() != AccountStatus.ACTIVE) {
            return false;
        }
        Instant revokedAt = access.revokedAt();
        // JWT 'iat' is epoch seconds; a token minted in the same second as the
        // revocation is treated as pre-revocation and rejected.
        return revokedAt == null || tokenIssuedAt.isAfter(revokedAt);
    }

    private AccountAccess loadAccountAccess(UUID userId) {
        AccountStatus status = userProfileRepository.findById(userId)
                .map(profile -> profile.getStatus())
                .orElse(null);
        if (status == null) {
            // Unknown user: deny access (token subject references a deleted account).
            return new AccountAccess(null, null);
        }
        Optional<UserTokenRevocation> revocation = userTokenRevocationRepository.findByUserId(userId);
        return new AccountAccess(status, revocation.map(UserTokenRevocation::getRevokedAt).orElse(null));
    }
}