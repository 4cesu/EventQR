package com.thedavelopers.eventqr.features.auth.model.dto;

/** Optional body for logout; lets the server end the refresh-token family too. */
public record LogoutRequest(String refreshToken) {
}
