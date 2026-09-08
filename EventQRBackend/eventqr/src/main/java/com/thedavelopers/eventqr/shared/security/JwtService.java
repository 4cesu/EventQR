package com.thedavelopers.eventqr.shared.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final int MAX_REVOKED_TOKENS = 25_000;

    private final SecretKey secretKey;
    private final Duration expiration;

    /**
     * Revoked-token denylist keyed by the raw signed JWT string. Entries live exactly
     * as long as the token's own remaining lifetime (computed at revocation), so a
     * logged-out client cannot replay a token until natural expiry. Memory is bounded
     * by {@link #MAX_REVOKED_TOKENS} and each entry self-expires.
     */
    private final Cache<String, Long> revokedTokens = Caffeine.newBuilder()
            .maximumSize(MAX_REVOKED_TOKENS)
            .expireAfter(new Expiry<String, Long>() {
                @Override
                public long expireAfterCreate(String key, Long ttlMillis, long currentTime) {
                    return TimeUnit.MILLISECONDS.toNanos(ttlMillis);
                }

                @Override
                public long expireAfterUpdate(String key, Long value, long currentTime, long currentDuration) {
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(String key, Long value, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured: set the JWT_SECRET environment variable");
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret is too weak: it must be at least 32 bytes (HS256), but was " + secretBytes.length + " bytes");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretBytes);
        this.expiration = Duration.ofMillis(expirationMs);
    }

    public String createToken(UUID userId, String email, AccountRole role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("userId", userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Adds the bearer token to the denylist until its natural expiration. Invalid or
     * already-expired tokens are ignored (nothing to revoke, no-op for idempotency).
     */
    public void revoke(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token == null) {
            return;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiresAt = claims.getExpiration();
            if (expiresAt == null) {
                return;
            }
            long ttlMillis = expiresAt.getTime() - System.currentTimeMillis();
            if (ttlMillis > 0) {
                revokedTokens.put(token, ttlMillis);
            }
        } catch (JwtException | IllegalArgumentException exception) {
            // Not a valid signed token: nothing to revoke.
        }
    }

    /** Returns true when the bearer token was revoked via {@link #revoke(String)}. */
    public boolean isRevoked(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        return token != null && revokedTokens.getIfPresent(token) != null;
    }

    public UUID extractUserIdFromBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing session token");
        }
        Claims claims = extractClaims(authorizationHeader);
        String userId = claims.get("userId", String.class);
        if (userId == null || userId.isBlank()) {
            userId = claims.getSubject();
        }
        return UUID.fromString(userId);
    }

    public AccountRole extractRoleFromBearer(String authorizationHeader) {
        Claims claims = extractClaims(authorizationHeader);
        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            throw new UnauthorizedException("Invalid or expired session");
        }
        try {
            return AccountRole.valueOf(role);
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Invalid or expired session");
        }
    }

    public String extractEmailFromBearer(String authorizationHeader) {
        Claims claims = extractClaims(authorizationHeader);
        String email = claims.get("email", String.class);
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Invalid or expired session");
        }
        return email;
    }

    private Claims extractClaims(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing session token");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new UnauthorizedException("Missing session token");
        }
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedException("Invalid or expired session");
        }
    }

    /** Raw token extraction without parsing, so revocation checks never parse/throw. */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        return token.isBlank() ? null : token;
    }
}

