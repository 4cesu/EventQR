package com.thedavelopers.eventqr.core.session

import android.content.Context
import com.thedavelopers.eventqr.core.api.ApiClient
import com.thedavelopers.eventqr.features.auth.model.dto.LogoutRequest
import com.thedavelopers.eventqr.features.registrations.RegistrationsCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

object SessionLogout {
    private const val REVOKE_TIMEOUT_MS = 5_000L

    /**
     * Revokes the current token on the backend, then clears local session state.
     * The revoke call must run first because AuthInterceptor reads the token from the
     * session. It is best-effort: if the network is down or slow, the user is still
     * signed out locally and the token expires on its own.
     */
    suspend fun signOut(context: Context) {
        val appContext = context.applicationContext
        val sessionManager = SessionManager(appContext)
        if (sessionManager.hasUsableToken()) {
            withTimeoutOrNull(REVOKE_TIMEOUT_MS) {
                try {
                    ApiClient.getService(appContext).logout(LogoutRequest(sessionManager.getRefreshToken()))
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    // Offline or server error: fall through to local sign-out.
                }
            }
        }
        RegistrationsCache.clear()
        sessionManager.clearSession()
    }
}
