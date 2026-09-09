package com.thedavelopers.eventqr.features.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thedavelopers.eventqr.features.auth.model.dto.LoginRequest;
import com.thedavelopers.eventqr.features.users.model.entity.UserProfile;
import com.thedavelopers.eventqr.features.users.repository.UserProfileRepository;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.constants.AccountStatus;
import com.thedavelopers.eventqr.shared.exceptions.UnauthorizedException;
import com.thedavelopers.eventqr.shared.security.JwtService;

class AuthServiceTest {

    private static final String EMAIL = "attendee@example.com";
    private static final String PASSWORD = "secret123";

    private UserProfileRepository userProfileRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userProfileRepository = mock(UserProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userProfileRepository, passwordEncoder, jwtService);
    }

    private UserProfile activeUser() {
        UserProfile p = new UserProfile();
        p.setId(UUID.randomUUID());
        p.setEmail(EMAIL);
        p.setFullName("Attendee User");
        p.setRole(AccountRole.ATTENDEE);
        p.setStatus(AccountStatus.ACTIVE);
        p.setPasswordHash("encoded-hash");
        return p;
    }

    private void stubActiveLogin(UserProfile user, boolean passwordMatches) {
        when(userProfileRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(passwordMatches);
    }

    @Test
    void disabledUserCannotLogin() {
        UserProfile user = activeUser();
        user.setStatus(AccountStatus.INACTIVE);
        stubActiveLogin(user, true);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account is disabled. Contact support.");
        verifyNoInteractions(jwtService);
    }

    @Test
    void suspendedUserCannotLogin() {
        UserProfile user = activeUser();
        user.setStatus(AccountStatus.SUSPENDED);
        stubActiveLogin(user, true);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account is disabled. Contact support.");
        verifyNoInteractions(jwtService);
    }

    @Test
    void activeUserCanLogin() {
        UserProfile user = activeUser();
        stubActiveLogin(user, true);
        when(jwtService.createToken(user.getId(), user.getEmail(), user.getRole())).thenReturn("token");

        var response = authService.login(new LoginRequest(EMAIL, PASSWORD));

        org.assertj.core.api.Assertions.assertThat(response.accessToken()).isEqualTo("token");
        verify(jwtService).createToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Test
    void pendingUserCanStillLogin() {
        // PENDING is not used by any registration flow today; do not break behavior.
        UserProfile user = activeUser();
        user.setStatus(AccountStatus.PENDING);
        stubActiveLogin(user, true);
        when(jwtService.createToken(user.getId(), user.getEmail(), user.getRole())).thenReturn("token");

        var response = authService.login(new LoginRequest(EMAIL, PASSWORD));

        org.assertj.core.api.Assertions.assertThat(response.accessToken()).isEqualTo("token");
    }

    @Test
    void wrongPasswordStillReportsGenericErrorEvenWhenDisabled() {
        // Status check must happen after password verification so the disabled state
        // is not disclosed to someone who does not know the password.
        UserProfile user = activeUser();
        user.setStatus(AccountStatus.INACTIVE);
        stubActiveLogin(user, false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
        verify(jwtService, never()).createToken(any(UUID.class), any(), any());
    }

    @Test
    void refreshTokenRejectsDisabledUser() {
        UserProfile user = activeUser();
        user.setStatus(AccountStatus.INACTIVE);
        when(userProfileRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refreshToken(user.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account is disabled. Contact support.");
        verifyNoInteractions(jwtService);
    }
}