package com.thedavelopers.eventqr.shared.security;

import java.time.Instant;
import java.util.Optional;

/**
 * Durable, shared storage for revoked bearer tokens. Keys are SHA-256 hex digests of the
 * signed token so a database leak never exposes a usable credential.
 */
public interface RevokedTokenStore {

    void save(String tokenHash, Instant expiresAt);

    /** The token's expiry when it has been revoked and is still within its lifetime. */
    Optional<Instant> findActive(String tokenHash);
}
