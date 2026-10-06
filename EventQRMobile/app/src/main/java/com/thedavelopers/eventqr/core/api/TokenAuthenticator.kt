package com.thedavelopers.eventqr.core.api

import com.thedavelopers.eventqr.core.session.TokenStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/** What the server said when asked to exchange a refresh token. */
sealed interface RefreshOutcome {
    data class Success(val accessToken: String, val refreshToken: String?) : RefreshOutcome

    /** The server understood and said no: the session is over. */
    data object Rejected : RefreshOutcome

    /** Offline, timed out or a server error: say nothing about the session's validity. */
    data object Unavailable : RefreshOutcome
}

fun interface RefreshCall {
    fun refresh(refreshToken: String): RefreshOutcome
}

/**
 * On a 401, trades the refresh token for a new access token and retries the request once, so an
 * expired access token is invisible to the user. Only a definite rejection from the server ends
 * the session; being offline never logs anyone out.
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshCall: RefreshCall,
    private val onSessionExpired: () -> Unit,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val failedRequest = response.request
        val failedHeader = failedRequest.header("Authorization") ?: return null
        if (isCredentialEndpoint(failedRequest.url.encodedPath) || responseCount(response) >= 2) {
            return null
        }
        val failedToken = failedHeader.removePrefix("Bearer ").trim()

        // One refresh at a time: refresh tokens are single use, so parallel 401s must not race.
        synchronized(lock) {
            val current = tokenStore.getAccessToken()
            if (!current.isNullOrBlank() && current != failedToken) {
                // Another request already refreshed while this one waited.
                return withToken(failedRequest, current)
            }
            val refreshToken = tokenStore.getRefreshToken()?.takeIf { it.isNotBlank() } ?: return null
            return when (val outcome = refreshCall.refresh(refreshToken)) {
                is RefreshOutcome.Success -> {
                    tokenStore.saveTokens(outcome.accessToken, outcome.refreshToken)
                    withToken(failedRequest, outcome.accessToken)
                }
                RefreshOutcome.Rejected -> {
                    tokenStore.clear()
                    onSessionExpired()
                    null
                }
                RefreshOutcome.Unavailable -> null
            }
        }
    }

    private fun withToken(request: Request, token: String): Request =
        request.newBuilder().header("Authorization", "Bearer $token").build()

    // A 401 from these means "wrong credentials", not "expired session".
    private fun isCredentialEndpoint(path: String): Boolean =
        path.endsWith("/auth/login") || path.endsWith("/auth/register") || path.endsWith("/auth/refresh")

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
