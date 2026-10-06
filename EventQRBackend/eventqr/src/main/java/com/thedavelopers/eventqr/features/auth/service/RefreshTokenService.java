package com.thedavelopers.eventqr.features.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thedavelopers.eventqr.features.auth.model.entity.RefreshToken;
import com.thedavelopers.eventqr.features.auth.repository.RefreshTokenRepository;
import com.thedavelopers.eventqr.shared.exceptions.UnauthorizedException;
import com.thedavelopers.eventqr.shared.security.UserTokenRevocationChecker;

/**
 * Issues and rotates single-use refresh tokens.
 *
 * <p>Presenting a token that was already used normally means it was stolen, so the whole
 * family is revoked. The one exception is a short retry window: a phone that lost the response
 * to a successful refresh will resend the same token, and that must not log the user out.
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    /** A used token presented again within this window is treated as a retry, not theft. */
    static final Duration RETRY_WINDOW = Duration.ofSeconds(30);

    private static final long PURGE_INTERVAL_MS = 60L * 60L * 1000L;

    private final SecureRandom random = new SecureRandom();
    private final RefreshTokenRepository repository;
    private final UserTokenRevocationChecker accountAccessChecker;
    private final Duration lifetime;

    public RefreshTokenService(RefreshTokenRepository repository,
                               UserTokenRevocationChecker accountAccessChecker,
                               @Value("${jwt.refresh-expiration-days:30}") long lifetimeDays) {
        this.repository = repository;
        this.accountAccessChecker = accountAccessChecker;
        this.lifetime = Duration.ofDays(lifetimeDays);
    }

    /** The outcome of a successful rotation. */
    public record Rotated(UUID userId, String refreshToken) {
    }

    /** Starts a new family for a fresh login and returns the raw refresh token. */
    @Transactional
    public String issueForLogin(UUID userId) {
        return issue(userId, UUID.randomUUID());
    }

    /**
     * Consumes {@code rawToken} and returns its successor. Throws {@link UnauthorizedException}
     * for unknown, expired, revoked or reused tokens. Reuse revokes the family, so this must
     * commit even though it throws.
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public Rotated rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Invalid or expired session");
        }
        Instant now = Instant.now();
        RefreshToken current = repository.findByTokenHash(sha256(rawToken.trim()))
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired session"));
        if (current.getRevokedAt() != null || !current.getExpiresAt().isAfter(now)) {
            throw new UnauthorizedException("Invalid or expired session");
        }
        // Same gate as access tokens: a disabled or suspended account, or a token issued before
        // such a freeze, can no longer be exchanged.
        if (!accountAccessChecker.isAccessAllowed(current.getUserId(), current.getCreatedAt())) {
            throw new UnauthorizedException("Invalid or expired session");
        }
        if (current.getUsedAt() != null) {
            if (current.getUsedAt().plus(RETRY_WINDOW).isBefore(now)) {
                repository.revokeFamily(current.getFamilyId(), now);
                log.warn("Refresh token reuse detected for user {}; revoked its whole session family",
                        current.getUserId());
                throw new UnauthorizedException("Invalid or expired session");
            }
        } else {
            current.setUsedAt(now);
        }
        return new Rotated(current.getUserId(), issue(current.getUserId(), current.getFamilyId()));
    }

    /** Ends the login that {@code rawToken} belongs to. Unknown tokens are ignored. */
    @Transactional
    public void revokeFamilyOf(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(sha256(rawToken.trim()))
                .ifPresent(token -> repository.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    /** Ends every login for the user, e.g. after a password change or reset. */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId, Instant.now());
    }

    @Scheduled(fixedDelay = PURGE_INTERVAL_MS, initialDelay = PURGE_INTERVAL_MS)
    @Transactional
    public void purgeExpired() {
        repository.deleteExpired(Instant.now());
    }

    private String issue(UUID userId, UUID familyId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = Instant.now();
        repository.save(new RefreshToken(userId, familyId, sha256(raw), now, now.plus(lifetime)));
        return raw;
    }

    static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
