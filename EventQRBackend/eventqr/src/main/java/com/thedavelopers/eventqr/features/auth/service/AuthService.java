package com.thedavelopers.eventqr.features.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thedavelopers.eventqr.features.auth.model.dto.LoginRequest;
import com.thedavelopers.eventqr.features.auth.model.dto.LoginResponse;
import com.thedavelopers.eventqr.features.users.model.entity.UserProfile;
import com.thedavelopers.eventqr.features.users.repository.UserProfileRepository;
import com.thedavelopers.eventqr.shared.exceptions.UnauthorizedException;
import com.thedavelopers.eventqr.shared.security.JwtService;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String UNUSABLE_HASH_PREFIX = "{UNUSABLE}";

    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        UserProfile userProfile = userProfileRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (userProfile.getPasswordHash() == null || userProfile.getPasswordHash().isBlank()
            || userProfile.getPasswordHash().startsWith(UNUSABLE_HASH_PREFIX)) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (!passwordEncoder.matches(request.password(), userProfile.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        String accessToken = jwtService.createToken(userProfile.getId(), userProfile.getEmail(), userProfile.getRole());
        return new LoginResponse(accessToken, userProfile.getId(), userProfile.getEmail(), userProfile.getFullName(),
                userProfile.getRole(), "Login successful");
    }

    /**
     * Re-issues a session token for an already authenticated user based on the
     * user's CURRENT role in the database, rather than the (potentially stale)
     * role embedded in the presented JWT. This lets a user pick up a role change
     * (e.g. an attendee upgraded to organizer after their event request is approved)
     * without forcing a logout/login cycle.
     */
    public LoginResponse refreshToken(UUID userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Invalid session"));
        String accessToken = jwtService.createToken(userProfile.getId(), userProfile.getEmail(), userProfile.getRole());
        return new LoginResponse(accessToken, userProfile.getId(), userProfile.getEmail(), userProfile.getFullName(),
                userProfile.getRole(), "Session refreshed");
    }
}
