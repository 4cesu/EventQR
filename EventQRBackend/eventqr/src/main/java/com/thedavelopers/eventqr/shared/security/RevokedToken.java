package com.thedavelopers.eventqr.shared.security;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A logged-out bearer token, identified by the SHA-256 hex digest of the signed JWT. */
@Getter
@Setter
@Entity
@Table(name = "revoked_token")
public class RevokedToken {

    @Id
    @Column(name = "token_hash", nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected RevokedToken() {
        // JPA
    }

    public RevokedToken(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}
