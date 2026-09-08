package com.thedavelopers.eventqr.features.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thedavelopers.eventqr.features.auth.model.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, Instant now);

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.userId = :userId AND p.used = false")
    int invalidateAllUnusedByUserId(UUID userId);

    /**
     * Bulk purge of expired tokens (used or unused). Called hourly by the scheduler so
     * the password_reset_tokens table cannot grow unbounded.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :cutoff")
    int deleteByExpiresAtBefore(@Param("cutoff") Instant cutoff);
}
