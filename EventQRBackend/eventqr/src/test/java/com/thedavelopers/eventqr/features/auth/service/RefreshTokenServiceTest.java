package com.thedavelopers.eventqr.features.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thedavelopers.eventqr.features.auth.model.entity.RefreshToken;
import com.thedavelopers.eventqr.features.auth.repository.RefreshTokenRepository;
import com.thedavelopers.eventqr.shared.exceptions.UnauthorizedException;
import com.thedavelopers.eventqr.shared.security.UserTokenRevocationChecker;

import static org.mockito.Mockito.mock;

class RefreshTokenServiceTest {

    private final Map<String, RefreshToken> byHash = new HashMap<>();
    private RefreshTokenRepository repository;
    private UserTokenRevocationChecker checker;
    private RefreshTokenService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenRepository.class);
        checker = mock(UserTokenRevocationChecker.class);
        when(checker.isAccessAllowed(any(), any())).thenReturn(true);
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            byHash.put(token.getTokenHash(), token);
            return token;
        });
        when(repository.findByTokenHash(any())).thenAnswer(invocation ->
                Optional.ofNullable(byHash.get(invocation.<String>getArgument(0))));
        service = new RefreshTokenService(repository, checker, 30);
    }

    private RefreshToken stored(String raw) {
        return byHash.get(RefreshTokenService.sha256(raw));
    }

    @Test
    void issuedTokenIsStoredAsAHashNotTheRawValue() {
        String raw = service.issueForLogin(userId);

        assertThat(byHash).doesNotContainKey(raw);
        assertThat(stored(raw)).isNotNull();
        assertThat(stored(raw).getTokenHash()).hasSize(64);
    }

    @Test
    void rotationMarksTheOldTokenUsedAndIssuesASuccessorInTheSameFamily() {
        String first = service.issueForLogin(userId);

        RefreshTokenService.Rotated rotated = service.rotate(first);

        assertThat(rotated.userId()).isEqualTo(userId);
        assertThat(rotated.refreshToken()).isNotEqualTo(first);
        assertThat(stored(first).getUsedAt()).isNotNull();
        assertThat(stored(rotated.refreshToken()).getFamilyId()).isEqualTo(stored(first).getFamilyId());
    }

    @Test
    void resendingATokenWithinTheRetryWindowIsTreatedAsARetry() {
        String first = service.issueForLogin(userId);
        service.rotate(first);

        RefreshTokenService.Rotated retry = service.rotate(first);

        assertThat(retry.refreshToken()).isNotBlank();
        verify(repository, never()).revokeFamily(any(), any());
    }

    @Test
    void reusingATokenAfterTheRetryWindowRevokesTheWholeFamily() {
        String first = service.issueForLogin(userId);
        service.rotate(first);
        RefreshToken old = stored(first);
        old.setUsedAt(Instant.now().minus(RefreshTokenService.RETRY_WINDOW).minusSeconds(5));

        assertThatThrownBy(() -> service.rotate(first)).isInstanceOf(UnauthorizedException.class);

        verify(repository).revokeFamily(any(UUID.class), any(Instant.class));
    }

    @Test
    void unknownBlankAndNullTokensAreRejected() {
        assertThatThrownBy(() -> service.rotate("not-a-token")).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.rotate("  ")).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.rotate(null)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void expiredAndRevokedTokensAreRejected() {
        String expiredRaw = service.issueForLogin(userId);
        RefreshToken expired = stored(expiredRaw);
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        String revokedRaw = service.issueForLogin(userId);
        stored(revokedRaw).setRevokedAt(Instant.now());

        assertThatThrownBy(() -> service.rotate(expiredRaw)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> service.rotate(revokedRaw)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void aDisabledAccountCannotRefresh() {
        String raw = service.issueForLogin(userId);
        when(checker.isAccessAllowed(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.rotate(raw)).isInstanceOf(UnauthorizedException.class);
        assertThat(stored(raw).getUsedAt()).isNull();
    }

    @Test
    void tokensLiveForTheConfiguredNumberOfDays() {
        String raw = service.issueForLogin(userId);

        Duration lifetime = Duration.between(stored(raw).getCreatedAt(), stored(raw).getExpiresAt());

        assertThat(lifetime).isEqualTo(Duration.ofDays(30));
    }

    @Test
    void logoutRevokesTheFamilyOfTheGivenToken() {
        String raw = service.issueForLogin(userId);

        service.revokeFamilyOf(raw);

        verify(repository).revokeFamily(any(UUID.class), any(Instant.class));
    }

    @Test
    void logoutWithoutATokenDoesNothing() {
        service.revokeFamilyOf(null);
        service.revokeFamilyOf("unknown");

        verify(repository, never()).revokeFamily(any(), any());
    }
}
