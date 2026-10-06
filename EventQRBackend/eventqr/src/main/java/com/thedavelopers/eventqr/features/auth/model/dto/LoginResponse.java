package com.thedavelopers.eventqr.features.auth.model.dto;

import java.util.UUID;

import com.thedavelopers.eventqr.shared.constants.AccountRole;

public record LoginResponse(String accessToken, UUID userId, String email, String fullName, AccountRole role,
                            String message, String refreshToken) {

    /** For callers that only deal in access tokens (no refresh token issued). */
    public LoginResponse(String accessToken, UUID userId, String email, String fullName, AccountRole role,
                         String message) {
        this(accessToken, userId, email, fullName, role, message, null);
    }
}
