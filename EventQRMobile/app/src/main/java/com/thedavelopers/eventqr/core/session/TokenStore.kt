package com.thedavelopers.eventqr.core.session

/** The slice of session storage the token refresher needs; lets it be tested without Android. */
interface TokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?

    /** Replaces the access token; keeps the stored refresh token when [refreshToken] is null. */
    fun saveTokens(accessToken: String, refreshToken: String?)

    fun clear()
}
